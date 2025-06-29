#include "lua_api.hpp"

#include <bit>
#include <filesystem>

#include <imgui.h>
#include <misc/cpp/imgui_stdlib.h>
#include <glm/glm.hpp>

#include "au_util.hpp"
#include "au_hash.hpp"
#include "api/au_api.hpp"

#include <glad/gl.h>

namespace {

	int lua_hash(lua_State* L) {
		size_t len;
		char const* data = lua_tolstring(L, 1, &len);
		std::uint32_t const result = aurora::fnv1a(data, len);
		lua_pushinteger(L, result);
		return 1;
	}

	int lua_string_unescape(lua_State* L) {
		std::size_t size;
		char const* bytes = luaL_checklstring(L, 1, &size);
		std::string const unescaped = aurora::unescape(std::string_view(bytes, size));
		lua_pushlstring(L, unescaped.data(), unescaped.size());
		return 1;
	}

	int lua_escape(lua_State* L) {
		std::size_t size;
		char const* bytes = luaL_checklstring(L, 1, &size);
		std::string const escaped = aurora::escape(std::string_view(bytes, size));
		lua_pushlstring(L, escaped.data(), escaped.size());
		return 1;
	}
}

#define DDSKTX_IMPLEMENT
#include "dds-ktx.h"
#include <iostream>

int api_ddsktx_parse(lua_State* L) {
	std::size_t size;
	char const* data = luaL_checklstring(L, 1, &size);

	ddsktx_texture_info tc = { 0 };
	ddsktx_error error;

	if (ddsktx_parse(&tc, data, static_cast<int>(size), &error)) {
		GLuint texture;
		glCreateTextures(GL_TEXTURE_2D, 1, &texture);
		GLenum format;

		switch (tc.format) {
		case DDSKTX_FORMAT_BC1:
			format = GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
			break;
		case DDSKTX_FORMAT_BC2:
			format = GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
			break;
		case DDSKTX_FORMAT_BC3:
			format = GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
			break;
		case DDSKTX_FORMAT_BGRA8:
			format = GL_BGRA;
			break;
		default:
			luaL_error(L, "Unsupported dds texture format: %d", tc.format);
			return 1;
		}

		ddsktx_sub_data subdata;
		ddsktx_get_sub(&tc, &subdata, data, static_cast<int>(size), 0, 0, 0);

		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		glTextureStorage2D(texture, 1, format, subdata.width, subdata.height);
		if (ddsktx_format_compressed(tc.format)) {
			glCompressedTextureSubImage2D(texture, 0, 0, 0, subdata.width, subdata.height, format, subdata.size_bytes, subdata.buff);
		}
		else {
			glTextureSubImage2D(texture, 0, 0, 0, subdata.width, subdata.height, format, GL_UNSIGNED_BYTE, subdata.buff);
		}

		glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

		lua_pushinteger(L, texture);
		return 1;
	}
	else {
		std::cout << error.msg << std::endl;
		lua_pushnil(L);
	}

	return 1;
}

void aurora::register_plugin_api(lua_State* L) {
	lua_newtable(L);
	lua_pushcfunction(L, &lua_hash);
	lua_setfield(L, -2, "hash");
	lua_pushcfunction(L, &lua_string_unescape);
	lua_setfield(L, -2, "unescape");
	lua_pushcfunction(L, &lua_escape);
	lua_setfield(L, -2, "escape");

	lua_pushcfunction(L, [](lua_State *L) -> int {
			lua_pushnumber(L, std::bit_cast<float>(static_cast<std::uint32_t>(lua_tointeger(L, 1))));
			return 1;
		});
	lua_setfield(L, -2, "bitcast_float");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		char const* path = luaL_checkstring(L, 1);

		auto const bytes = read_file(path);

		if (!bytes.has_value()) {
			lua_pushnil(L);
		}
		else {
			lua_pushlstring(L, reinterpret_cast<const char *>(bytes->data()), bytes->size());
		}

		return 1;
	});
	lua_setfield(L, -2, "read_file");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		char const* path = luaL_checkstring(L, 1);

		std::size_t size;
		char const* data = luaL_checklstring(L, 2, &size);

		bool const success = write_file(path, std::as_bytes(std::span(data, size)));
		lua_pushboolean(L, success);

		return 1;
	});
	lua_setfield(L, -2, "write_file");

	lua_pushcfunction(L, api_ddsktx_parse);
	lua_setfield(L, -2, "ddsktx_parse");

	aurora::api_register_filesystem(L);
	lua_setfield(L, -2, "filesystem");

	lua_setglobal(L, "Aurora");

	aurora::api_register_imgui(L);
	lua_setglobal(L, "ImGui");

	aurora::api_register_glm(L);
	lua_setglobal(L, "glm");

	lua_newtable(L);
	lua_pushcfunction(L, [](lua_State *L) -> int {
		GLuint handle;
		glCreateVertexArrays(1, &handle);
		lua_pushinteger(L, handle);
		return 1;
	});
	lua_setfield(L, -2, "CreateVertexArrays");
	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const handle = static_cast<GLuint>(luaL_checkinteger(L, 1));
		glDeleteVertexArrays(1, &handle);
		return 0;
	});
	lua_setfield(L, -2, "DeleteVertexArrays");
	lua_pushcfunction(L, [](lua_State *L) -> int {
		GLuint handle;
		glCreateBuffers(1, &handle);
		lua_pushinteger(L, handle);
		return 1;
	});
	lua_setfield(L, -2, "CreateBuffers");
	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const handle = static_cast<GLuint>(luaL_checkinteger(L, 1));
		glDeleteBuffers(1, &handle);
		return 0;
	});
	lua_setfield(L, -2, "DeleteBuffers");
	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const buffer = static_cast<GLuint>(luaL_checkinteger(L, 1));
		auto const size = static_cast<GLsizeiptr>(luaL_checkinteger(L, 2));
		char const* data = luaL_checkstring(L, 3);
		auto const flags = static_cast<GLbitfield>(luaL_checkinteger(L, 4));
		glNamedBufferStorage(buffer, size, data, flags);
		return 0;
	});
	lua_setfield(L, -2, "NamedBufferStorage");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const target = static_cast<GLenum>(luaL_checkinteger(L, 1));
		GLuint handle;
		glCreateTextures(target, 1, &handle);
		lua_pushinteger(L, handle);
		return 1;
	});
	lua_setfield(L, -2, "CreateTextures");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const handle = static_cast<GLuint>(luaL_checkinteger(L, 1));
		glDeleteTextures(1, &handle);
		return 0;
	});
	lua_setfield(L, -2, "DeleteTextures");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const texture = static_cast<GLuint>(luaL_checkinteger(L, 1));
		auto const levels = static_cast<GLsizei>(luaL_checkinteger(L, 2));
		auto const internalformat = static_cast<GLenum>(luaL_checkinteger(L, 3));
		auto const width = static_cast<GLsizei>(luaL_checkinteger(L, 4));
		auto const height = static_cast<GLsizei>(luaL_checkinteger(L, 5));
		glTextureStorage2D(texture, levels, internalformat, width, height);
		return 0;
	});
	lua_setfield(L, -2, "TextureStorage2D");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		glBindFramebuffer(static_cast<GLenum>(luaL_checkinteger(L, 1)), static_cast<GLuint>(luaL_checkinteger(L, 2)));
		return 0;
	});
	lua_setfield(L, -2, "BindFramebuffer");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		GLuint framebuffer;
		glCreateFramebuffers(1, &framebuffer);
		lua_pushinteger(L, framebuffer);
		return 1;
	});
	lua_setfield(L, -2, "CreateFramebuffers");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const framebuffer = static_cast<GLuint>(luaL_checkinteger(L, 1));
		glDeleteFramebuffers(1, &framebuffer);
		return 0;
	});
	lua_setfield(L, -2, "DeleteFramebuffers");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const framebuffer = static_cast<GLuint>(luaL_checkinteger(L, 1));
		auto const attachment = static_cast<GLenum>(luaL_checkinteger(L, 2));
		auto const texture = static_cast<GLuint>(luaL_checkinteger(L, 3));
		auto const level = static_cast<GLint>(luaL_checkinteger(L, 4));
		glNamedFramebufferTexture(framebuffer, attachment, texture, level);
		return 0;
	});
	lua_setfield(L, -2, "NamedFramebufferTexture");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const framebuffer = static_cast<GLuint>(luaL_checkinteger(L, 1));
		auto const attachment = static_cast<GLenum>(luaL_checkinteger(L, 2));
		auto const renderbuffer = static_cast<GLuint>(luaL_checkinteger(L, 3));
		glNamedFramebufferRenderbuffer(framebuffer, attachment, GL_RENDERBUFFER, renderbuffer);
		return 0;
	});
	lua_setfield(L, -2, "NamedFramebufferRenderbuffer");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		GLuint renderbuffer;
		glCreateRenderbuffers(1, &renderbuffer);
		lua_pushinteger(L, renderbuffer);
		return 1;
	});
	lua_setfield(L, -2, "CreateRenderbuffers");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const renderbuffer = static_cast<GLuint>(luaL_checkinteger(L, 1));
		glDeleteRenderbuffers(1, &renderbuffer);
		return 0;
	});
	lua_setfield(L, -2, "DeleteRenderbuffers");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const renderbuffer = static_cast<GLuint>(luaL_checkinteger(L, 1));
		auto const internalformat = static_cast<GLenum>(luaL_checkinteger(L, 2));
		auto const width = static_cast<GLsizei>(luaL_checkinteger(L, 3));
		auto const height = static_cast<GLsizei>(luaL_checkinteger(L, 4));
		glNamedRenderbufferStorage(renderbuffer, internalformat, width, height);
		return 0;
	});
	lua_setfield(L, -2, "NamedRenderbufferStorage");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const x = static_cast<GLint>(luaL_checkinteger(L, 1));
		auto const y = static_cast<GLint>(luaL_checkinteger(L, 2));
		auto const width = static_cast<GLsizei>(luaL_checkinteger(L, 3));
		auto const height = static_cast<GLsizei>(luaL_checkinteger(L, 4));
		glViewport(x, y, width, height);
		return 0;
	});
	lua_setfield(L, -2, "Viewport");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const vao = static_cast<GLuint>(luaL_checkinteger(L, 1));
		glBindVertexArray(vao);
		return 0;
	});
	lua_setfield(L, -2, "BindVertexArray");
	lua_pushcfunction(L, [](lua_State *L) -> int {
	                  auto const vao = static_cast<GLuint>(luaL_checkinteger(L, 1));
	                  auto const bindingindex = static_cast<GLuint>(luaL_checkinteger(L, 2));
	                  auto const buffer = static_cast<GLuint>(luaL_checkinteger(L, 3));
	                  auto const offset = static_cast<GLintptr>(luaL_checkinteger(L, 4));
	                  auto const stride = static_cast<GLsizei>(luaL_checkinteger(L, 5));
	                  glVertexArrayVertexBuffer(vao, bindingindex, buffer, offset, stride);
	                  return 0;
	                  });

	lua_setfield(L, -2, "VertexArrayVertexBuffer");

	lua_pushcfunction(L, [](lua_State *L) -> int {
	                  auto const vao = static_cast<GLuint>(luaL_checkinteger(L, 1));
	                  auto const index = static_cast<GLuint>(luaL_checkinteger(L, 2));
	                  glEnableVertexArrayAttrib(vao, index);
	                  return 0;
	                  });

	lua_setfield(L, -2, "EnableVertexArrayAttrib");

	lua_pushcfunction(L, [](lua_State *L) -> int {
					  auto const mode = static_cast<GLenum>(luaL_checkinteger(L, 1));
					  auto const first = static_cast<GLint>(luaL_checkinteger(L, 2));
					  auto const count = static_cast<GLsizei>(luaL_checkinteger(L, 3));
					  glDrawArrays(mode, first, count);
					  return 0;
					  });

	lua_setfield(L, -2, "DrawArrays");

	lua_pushcfunction(L, [](lua_State *L) -> int {
	                  auto const vao = static_cast<GLuint>(luaL_checkinteger(L, 1));
	                  auto const attribindex = static_cast<GLuint>(luaL_checkinteger(L, 2));
	                  auto const size = static_cast<GLint>(luaL_checkinteger(L, 3));
	                  auto const type = static_cast<GLenum>(luaL_checkinteger(L, 4));
	                  auto const normalized = static_cast<GLboolean>(lua_toboolean(L, 5));
	                  auto const relativeoffset = static_cast<GLuint>(luaL_checkinteger(L, 6));
	                  glVertexArrayAttribFormat(vao, attribindex, size, type, normalized, relativeoffset);
	                  return 0;
	                  });

	lua_setfield(L, -2, "VertexArrayAttribFormat");

	lua_pushcfunction(L, [](lua_State *L) -> int {
					  auto const vao = static_cast<GLuint>(luaL_checkinteger(L, 1));
					  auto const attribindex = static_cast<GLuint>(luaL_checkinteger(L, 2));
					  auto const bindingindex = static_cast<GLuint>(luaL_checkinteger(L, 3));
					  glVertexArrayAttribBinding(vao, attribindex, bindingindex);
					  return 0;
					  });

	lua_setfield(L, -2, "VertexArrayAttribBinding");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		auto const framebuffer = static_cast<GLuint>(luaL_checkinteger(L, 1));
		auto const buffer = static_cast<GLenum>(luaL_checkinteger(L, 2));
		auto const drawbuffer = static_cast<GLint>((L, 3));

		float value[4];
		lua_pushinteger(L, 1); lua_rawget(L, 4); value[0] = static_cast<float>(lua_tonumber(L, -1)); lua_pop(L, 1);

		if (buffer == GL_COLOR) {
			lua_pushinteger(L, 2); lua_rawget(L, 4); value[1] = static_cast<float>(lua_tonumber(L, -1)); lua_pop(L, 1);
			lua_pushinteger(L, 3); lua_rawget(L, 4); value[2] = static_cast<float>(lua_tonumber(L, -1)); lua_pop(L, 1);
			lua_pushinteger(L, 4); lua_rawget(L, 4); value[3] = static_cast<float>(lua_tonumber(L, -1)); lua_pop(L, 1);
		}

		glClearNamedFramebufferfv(framebuffer, buffer, drawbuffer, value);
		return 0;
	});
	lua_setfield(L, -2, "ClearNamedFramebufferfv");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		              auto const type = static_cast<GLenum>(luaL_checkinteger(L, 1));
					  auto const shader = static_cast<GLuint>(glCreateShader(type));
					  lua_pushinteger(L, shader);
					  return 1;
					  });
	lua_setfield(L, -2, "CreateShader");

	lua_pushcfunction(L, [](lua_State *L) -> int {
					  auto const shader = static_cast<GLuint>(luaL_checkinteger(L, 1));
					  std::size_t size;
					  char const* data = luaL_checklstring(L, 2, &size);
			          auto const isize = static_cast<GLint>(size);
					  glShaderSource(shader, 1, &data, &isize);
					  return 0;
					  });
	lua_setfield(L, -2, "ShaderSource");

	lua_pushcfunction(L, [](lua_State *L) -> int {
					  auto const shader = static_cast<GLuint>(luaL_checkinteger(L, 1));
					  glCompileShader(shader);
					  return 0;
					  });
	lua_setfield(L, -2, "CompileShader");

	lua_pushcfunction(L, [](lua_State *L) -> int {
					  auto const program = static_cast<GLuint>(luaL_checkinteger(L, 1));
				      auto const shader = static_cast<GLuint>(luaL_checkinteger(L, 2));
					  glAttachShader(program, shader);
					  return 0;
					  });
	lua_setfield(L, -2, "AttachShader");

	lua_pushcfunction(L, [](lua_State *L) -> int {
					  auto const program = static_cast<GLuint>(luaL_checkinteger(L, 1));
					  auto const shader = static_cast<GLuint>(luaL_checkinteger(L, 2));
					  glDetachShader(program, shader);
					  return 0;
					  });
	lua_setfield(L, -2, "DetachShader");

	lua_pushcfunction(L, [](lua_State *L) -> int {
					  auto const shader = static_cast<GLuint>(luaL_checkinteger(L, 1));
					  glDeleteShader(shader);
					  return 0;
					  });
	lua_setfield(L, -2, "DeleteShader");

	lua_pushcfunction(L, [](lua_State *L) -> int {
						  GLuint const program = glCreateProgram();
						  lua_pushinteger(L, program);
						  return 1;
						  });
	lua_setfield(L, -2, "CreateProgram");

	lua_pushcfunction(L, [](lua_State *L) -> int {
						auto const program = static_cast<GLuint>(luaL_checkinteger(L, 1));
						  glLinkProgram(program);
						  return 0;
						  });
	lua_setfield(L, -2, "LinkProgram");

	lua_pushcfunction(L, [](lua_State *L) -> int {
						auto const program = static_cast<GLuint>(luaL_checkinteger(L, 1));
						  glUseProgram(program);
						  return 0;
						  });
	lua_setfield(L, -2, "UseProgram");

	lua_pushcfunction(L, [](lua_State *L) -> int {
						auto const program = static_cast<GLuint>(luaL_checkinteger(L, 1));
						  glDeleteProgram(program);
						  return 0;
						  });
	lua_setfield(L, -2, "DeleteProgram");

	lua_pushcfunction(L, [](lua_State *L) -> int {
	                  auto const program = static_cast<GLuint>(luaL_checkinteger(L, 1));
	                  char const* name = luaL_checkstring(L, 2);
	                  auto const location = static_cast<GLint>(glGetUniformLocation(program, name));
	                  lua_pushinteger(L, location);
	                  return 1;
	                  });
	lua_setfield(L, -2, "GetUniformLocation");

	lua_pushcfunction(L, [](lua_State *L) -> int {
	                  auto const program = static_cast<GLuint>(luaL_checkinteger(L, 1));
	                  auto const location = static_cast<GLint>(luaL_checkinteger(L, 2));
	                  auto const count = static_cast<GLsizei>(luaL_checkinteger(L, 3));
	                  auto const transpose = static_cast<GLboolean>(lua_toboolean(L, 4));

	                  auto *data = static_cast<float*>(alloca(count * sizeof(glm::mat4)));

	                  for (int i = 0; i < count * 16; ++i) {
		                  lua_rawgeti(L, 5, i + 1);
		                  data[i] = static_cast<float>(lua_tonumber(L, -1));
		                  lua_pop(L, 1);
	                  }

	                  glProgramUniformMatrix4fv(program, location, count, transpose, data);
	                  return 0;
	                  });
	lua_setfield(L, -2, "ProgramUniformMatrix4fv");

	lua_pushcfunction(L, [](lua_State *L) -> int {
                      auto const vao = static_cast<GLuint>(luaL_checkinteger(L, 1));
					  auto const ebo = static_cast<GLuint>(luaL_checkinteger(L, 2));
					  glVertexArrayElementBuffer(vao, ebo);
					  return 0;
					  });

	lua_setfield(L, -2, "VertexArrayElementBuffer");

	lua_pushcfunction(L, [](lua_State *L) -> int {
	                  auto const mode = static_cast<GLenum>(luaL_checkinteger(L, 1));
	                  auto const count = static_cast<GLsizei>(luaL_checkinteger(L, 2));
	                  auto const type = static_cast<GLenum>(luaL_checkinteger(L, 3));
	                  auto const *indices = reinterpret_cast<void*>(static_cast<std::uintptr_t>(luaL_checkinteger(L, 4))
	                  );
	                  glDrawElements(mode, count, type, indices);
	                  return 0;
	                  });

	lua_setfield(L, -2, "DrawElements");



	lua_setglobal(L, "gl");

	lua_newtable(L);
#define AU_IMPL_GL_EXPAND(L, name) lua_pushinteger(L, GL_ ## name); lua_setfield(L, -2, #name)
	AU_IMPL_GL_EXPAND(L, TEXTURE_2D);
	AU_IMPL_GL_EXPAND(L, RGBA8);
	AU_IMPL_GL_EXPAND(L, FRAMEBUFFER);
	AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT0);
	AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT1);
	AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT2);
	AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT3);
	AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT4);
	AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT5);
	AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT6);
	AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT7);
	AU_IMPL_GL_EXPAND(L, DEPTH_ATTACHMENT);
	AU_IMPL_GL_EXPAND(L, DEPTH_COMPONENT24);
	AU_IMPL_GL_EXPAND(L, DEPTH_COMPONENT32);
	AU_IMPL_GL_EXPAND(L, DEPTH_COMPONENT32F);
	AU_IMPL_GL_EXPAND(L, COLOR);
	AU_IMPL_GL_EXPAND(L, DEPTH);
	AU_IMPL_GL_EXPAND(L, STENCIL);
	AU_IMPL_GL_EXPAND(L, NONE);
	AU_IMPL_GL_EXPAND(L, FLOAT);
	AU_IMPL_GL_EXPAND(L, TRIANGLE_STRIP);
	AU_IMPL_GL_EXPAND(L, TRIANGLES);
	AU_IMPL_GL_EXPAND(L, VERTEX_SHADER);
	AU_IMPL_GL_EXPAND(L, FRAGMENT_SHADER);
	AU_IMPL_GL_EXPAND(L, UNSIGNED_SHORT);
	AU_IMPL_GL_EXPAND(L, UNSIGNED_INT);
#undef AU_IMPL_GL_EXPAND
	lua_setglobal(L, "GL");
}