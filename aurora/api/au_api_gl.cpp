#include "au_api.hpp"

#include <glad/gl.h>

#include "spdlog/fmt/bundled/args.h"

namespace {
    /// VERTEXARRAY //

    int CreateVertexArrays(lua_State *L) {
        auto const n = static_cast<GLsizei>(luaL_checkinteger(L, 1));
        luaL_checktype(L, 2, LUA_TLIGHTUSERDATA);
        glCreateVertexArrays(n, static_cast<GLuint*>(lua_touserdata(L, 2)));
        return 0;
    }

    int DeleteVertexArrays(lua_State *L) {
        auto const n = static_cast<GLsizei>(luaL_checkinteger(L, 1));
        luaL_checktype(L, 2, LUA_TLIGHTUSERDATA);
        glDeleteVertexArrays(n, static_cast<GLuint*>(lua_touserdata(L, 2)));
        return 0;
    }

    int BindVertexArray(lua_State *L) {
        GLuint const array = luaL_checkinteger(L, 1);
        glBindVertexArray(array);
        return 0;
    }

    int VertexArrayVertexBuffer(lua_State *L) {
        GLuint const vaobj = luaL_checkinteger(L, 1);
        GLuint const bindingindex = luaL_checkinteger(L, 2);
        GLuint const buffer = luaL_checkinteger(L, 3);
        GLintptr const offset = luaL_checkinteger(L, 4);
        auto const stride = static_cast<GLsizei>(luaL_checkinteger(L, 5));
        glVertexArrayVertexBuffer(vaobj, bindingindex, buffer, offset, stride);
        return 0;
    }

    int VertexArrayElementBuffer(lua_State *L) {
        GLuint const vaobj = luaL_checkinteger(L, 1);
        GLuint const buffer = luaL_checkinteger(L, 2);
        glVertexArrayElementBuffer(vaobj, buffer);
        return 0;
    }

    int EnableVertexArrayAttrib(lua_State *L) {
        GLuint const vaobj = luaL_checkinteger(L, 1);
        GLuint const index = luaL_checkinteger(L, 2);
        glEnableVertexArrayAttrib(vaobj, index);
        return 0;
    }

    int VertexArrayAttribFormat(lua_State *L) {
        GLuint const vaobj = luaL_checkinteger(L, 1);
        GLuint const attribindex = luaL_checkinteger(L, 2);
        auto const size = static_cast<GLint>(luaL_checkinteger(L, 3));
        GLenum const type = luaL_checkinteger(L, 4);
        GLboolean const normalized = lua_toboolean(L, 5);
        GLuint const relativeoffset = luaL_checkinteger(L, 6);
        glVertexArrayAttribFormat(vaobj, attribindex, size, type, normalized, relativeoffset);
        return 0;
    }

    int VertexArrayAttribBinding(lua_State *L) {
        GLuint const vaobj = luaL_checkinteger(L, 1);
        GLuint const attribindex = luaL_checkinteger(L, 2);
        GLuint const bindingindex = luaL_checkinteger(L, 3);
        glVertexArrayAttribBinding(vaobj, attribindex, bindingindex);
        return 0;
    }

    /// BUFFER //

    int CreateBuffers(lua_State *L) {
        auto const n = static_cast<GLsizei>(luaL_checkinteger(L, 1));
        luaL_checktype(L, 2, LUA_TLIGHTUSERDATA);
        glCreateBuffers(n, static_cast<GLuint*>(lua_touserdata(L, 2)));
        return 0;
    }

    int DeleteBuffers(lua_State *L) {
        auto const n = static_cast<GLsizei>(luaL_checkinteger(L, 1));
        luaL_checktype(L, 2, LUA_TLIGHTUSERDATA);
        glDeleteBuffers(n, static_cast<GLuint*>(lua_touserdata(L, 2)));
        return 0;
    }

    int NamedBufferStorage(lua_State *L) {
        GLuint const buffer = luaL_checkinteger(L, 1);
        GLsizeiptr const size = luaL_checkinteger(L, 2);
        luaL_checktype(L, 3, LUA_TLIGHTUSERDATA);
        void const *data = lua_touserdata(L, 3);
        GLbitfield const flags = luaL_checkinteger(L, 4);
        glNamedBufferStorage(buffer, size, data, flags);
        return 0;
    }

    /// SHADER //

    int CreateShader(lua_State *L) {
        GLenum const shaderType = luaL_checkinteger(L, 1);
        lua_pushinteger(L, glCreateShader(shaderType));
        return 1;
    }

    int DeleteShader(lua_State *L) {
        glDeleteShader(luaL_checkinteger(L, 1));
        return 0;
    }

    int ShaderSource(lua_State *L) {
        GLuint const shader = luaL_checkinteger(L, 1);
        auto const count = static_cast<GLsizei>(luaL_checkinteger(L, 2));
        luaL_checktype(L, 3, LUA_TLIGHTUSERDATA);
        auto const string = static_cast<GLchar const**>(lua_touserdata(L, 3));
        luaL_checktype(L, 4, LUA_TLIGHTUSERDATA);
        auto const length = static_cast<GLint const*>(lua_touserdata(L, 4));
        glShaderSource(shader, count, string, length);
        return 0;
    }

    int CompileShader(lua_State *L) {
        GLuint const shader = luaL_checkinteger(L, 1);
        glCompileShader(shader);
        return 0;
    }

    /// PROGRAM //

    int CreateProgram(lua_State *L) {
        lua_pushinteger(L, glCreateProgram());
        return 1;
    }

    int DeleteProgram(lua_State *L) {
        glDeleteProgram(luaL_checkinteger(L, 1));
        return 0;
    }

    int UseProgram(lua_State *L) {
        GLuint const program = luaL_checkinteger(L, 1);
        glUseProgram(program);
        return 0;
    }

    int AttachShader(lua_State *L) {
        GLuint const program = luaL_checkinteger(L, 1);
        GLuint const shader = luaL_checkinteger(L, 2);
        glAttachShader(program, shader);
        return 0;
    }

    int DetachShader(lua_State *L) {
        GLuint const program = luaL_checkinteger(L, 1);
        GLuint const shader = luaL_checkinteger(L, 2);
        glDetachShader(program, shader);
        return 0;
    }

    int LinkProgram(lua_State *L) {
        GLuint const program = luaL_checkinteger(L, 1);
        glLinkProgram(program);
        return 0;
    }

    int GetUniformLocation(lua_State *L) {
        GLuint const program = luaL_checkinteger(L, 1);



        if (int const type = lua_type(L, 2); !(type == LUA_TSTRING || type == LUA_TLIGHTUSERDATA)) {
            luaL_typeerror(L, 2, "userdata|string");
        }

        GLchar const* name;
        if (lua_islightuserdata(L, 2)) name = static_cast<GLchar*>(lua_touserdata(L, 2));
        else name = lua_tostring(L, 2);

        GLint const location = glGetUniformLocation(program, name);
        lua_pushinteger(L, location);
        return 1;
    }

    int ProgramUniformMatrix4fv(lua_State *L) {
        GLuint const program = luaL_checkinteger(L, 1);
        auto const location = static_cast<GLint>(luaL_checkinteger(L, 2));
        auto const count = static_cast<GLsizei>(luaL_checkinteger(L, 3));
        GLboolean const transpose = lua_toboolean(L, 4);

        GLfloat *value;

        if (lua_islightuserdata(L, 5)) {
            value = static_cast<GLfloat*>(lua_touserdata(L, 5));
        }
        else if (lua_istable(L, 5)) {
            auto const elementCount = lua_rawlen(L, 5);
            value = static_cast<GLfloat*>(alloca(elementCount * sizeof(GLfloat)));

            for (int i = 0; i < elementCount; ++i) {
                lua_rawgeti(L, 5, i + 1);
                value[i] = lua_tonumber(L, -1);
                lua_pop(L, 1);
            }
        }
        else
            luaL_typeerror(L, 5, "userdata|table");

        glProgramUniformMatrix4fv(program, location, count, transpose, value);
        return 0;
    }

    /// TEXTURE //

    int CreateTextures(lua_State *L) {
        GLenum const target = luaL_checkinteger(L, 1);
        auto const n = static_cast<GLsizei>(luaL_checkinteger(L, 2));
        luaL_checktype(L, 3, LUA_TLIGHTUSERDATA);
        glCreateTextures(target, n, static_cast<GLuint*>(lua_touserdata(L, 3)));
        return 0;
    }

    int DeleteTextures(lua_State *L) {
        auto const n = static_cast<GLsizei>(luaL_checkinteger(L, 1));
        luaL_checktype(L, 2, LUA_TLIGHTUSERDATA);
        glDeleteTextures(n, static_cast<GLuint*>(lua_touserdata(L, 2)));
        return 0;
    }

    int TextureStorage2D(lua_State *L) {
        GLuint const texture = luaL_checkinteger(L, 1);
        auto const levels = static_cast<GLsizei>(luaL_checkinteger(L, 2));
        GLenum const internalFormat = luaL_checkinteger(L, 3);
        auto const width = static_cast<GLsizei>(luaL_checkinteger(L, 4));
        auto const height = static_cast<GLsizei>(luaL_checkinteger(L, 5));
        glTextureStorage2D(texture, levels, internalFormat, width, height);
        return 0;
    }

    /// RENDERBUFFER //

    int CreateRenderbuffers(lua_State *L) {
        auto const n = static_cast<GLsizei>(luaL_checkinteger(L, 1));
        luaL_checktype(L, 2, LUA_TLIGHTUSERDATA);
        glCreateRenderbuffers(n, static_cast<GLuint*>(lua_touserdata(L, 2)));
        return 0;
    }

    int DeleteRenderbuffers(lua_State *L) {
        auto const n = static_cast<GLsizei>(luaL_checkinteger(L, 1));
        luaL_checktype(L, 2, LUA_TLIGHTUSERDATA);
        glDeleteRenderbuffers(n, static_cast<GLuint*>(lua_touserdata(L, 2)));
        return 0;
    }

    int NamedRenderbufferStorage(lua_State *L) {
        GLuint const renderbuffer = luaL_checkinteger(L, 1);
        GLenum const internalformat = luaL_checkinteger(L, 2);
        auto const width = static_cast<GLsizei>(luaL_checkinteger(L, 3));
        auto const height = static_cast<GLsizei>(luaL_checkinteger(L, 4));
        glNamedRenderbufferStorage(renderbuffer, internalformat, width, height);
        return 0;
    }

    /// FRAMEBUFFER //

    int CreateFramebuffers(lua_State *L) {
        auto const n = static_cast<GLsizei>(luaL_checkinteger(L, 1));
        luaL_checktype(L, 2, LUA_TLIGHTUSERDATA);
        glCreateFramebuffers(n, static_cast<GLuint*>(lua_touserdata(L, 2)));
        return 0;
    }

    int DeleteFramebuffers(lua_State *L) {
        auto const n = static_cast<GLsizei>(luaL_checkinteger(L, 1));
        luaL_checktype(L, 2, LUA_TLIGHTUSERDATA);
        glDeleteFramebuffers(n, static_cast<GLuint*>(lua_touserdata(L, 2)));
        return 0;
    }

    int BindFramebuffer(lua_State *L) {
        GLenum const target = luaL_checkinteger(L, 1);
        GLuint const framebuffer = luaL_checkinteger(L, 2);
        glBindFramebuffer(target, framebuffer);
        return 0;
    }

    int NamedFramebufferTexture(lua_State *L) {
        GLuint const framebuffer = luaL_checkinteger(L, 1);
        GLenum const attachment = luaL_checkinteger(L, 2);
        GLuint const texture = luaL_checkinteger(L, 3);
        auto const level = static_cast<GLint>(luaL_checkinteger(L, 4));
        glNamedFramebufferTexture(framebuffer, attachment, texture, level);
        return 0;
    }

    int NamedFramebufferRenderbuffer(lua_State *L) {
        GLuint const framebuffer = luaL_checkinteger(L, 1);
        GLenum const attachment = luaL_checkinteger(L, 2);
        GLenum const renderbuffertarget = luaL_checkinteger(L, 3);
        GLuint const renderbuffer = luaL_checkinteger(L, 4);
        glNamedFramebufferRenderbuffer(framebuffer, attachment, renderbuffertarget, renderbuffer);
        return 0;
    }

    int ClearNamedFramebufferfv(lua_State *L) {
        GLuint const framebuffer = luaL_checkinteger(L, 1);
        GLenum const buffer = luaL_checkinteger(L, 2);
        auto const drawbuffer = static_cast<GLint>(luaL_checkinteger(L, 3));

        GLfloat *value;

        if (lua_islightuserdata(L, 4)) {
            value = static_cast<GLfloat*>(lua_touserdata(L, 4));
        }
        else if (lua_istable(L, 4)) {
            auto const elementCount = lua_rawlen(L, 4);
            value = static_cast<GLfloat*>(alloca(elementCount * sizeof(GLfloat)));

            for (int i = 0; i < elementCount; ++i) {
                lua_rawgeti(L, 4, i + 1);
                value[i] = lua_tonumber(L, -1);
                lua_pop(L, 1);
            }
        }
        else
            luaL_typeerror(L, 4, "userdata|table");

        glClearNamedFramebufferfv(framebuffer, buffer, drawbuffer, value);
        return 0;
    }

    /// OTHER ///

    int Viewport(lua_State *L) {
        auto const x = static_cast<GLint>(luaL_checkinteger(L, 1));
        auto const y = static_cast<GLint>(luaL_checkinteger(L, 2));
        auto const width = static_cast<GLsizei>(luaL_checkinteger(L, 3));
        auto const height = static_cast<GLsizei>(luaL_checkinteger(L, 4));
        glViewport(x, y, width, height);
        return 0;
    }

    int DrawArrays(lua_State *L) {
        GLenum const mode = luaL_checkinteger(L, 1);
        auto const first = static_cast<GLint>(luaL_checkinteger(L, 2));
        auto const count = static_cast<GLsizei>(luaL_checkinteger(L, 3));
        glDrawArrays(mode, first, count);
        return 0;
    }

    int DrawElements(lua_State *L) {
        GLenum const mode = luaL_checkinteger(L, 1);
        auto const count = static_cast<GLsizei>(luaL_checkinteger(L, 2));
        GLenum const type = luaL_checkinteger(L, 3);
        luaL_checktype(L, 4, LUA_TLIGHTUSERDATA);
        auto const indices = static_cast<void const*>(lua_touserdata(L, 4));
        glDrawElements(mode, count, type, indices);
        return 0;
    }
}

void aurora::api_register_gl(lua_State *L) {
    lua_newtable(L);
    AU_IMPL_API_REGISTER(CreateVertexArrays);
    AU_IMPL_API_REGISTER(DeleteVertexArrays);
    AU_IMPL_API_REGISTER(BindVertexArray);
    AU_IMPL_API_REGISTER(EnableVertexArrayAttrib);
    AU_IMPL_API_REGISTER(VertexArrayVertexBuffer);
    AU_IMPL_API_REGISTER(VertexArrayElementBuffer);
    AU_IMPL_API_REGISTER(VertexArrayAttribFormat);
    AU_IMPL_API_REGISTER(VertexArrayAttribBinding);

    AU_IMPL_API_REGISTER(CreateBuffers);
    AU_IMPL_API_REGISTER(DeleteBuffers);
    AU_IMPL_API_REGISTER(NamedBufferStorage);

    AU_IMPL_API_REGISTER(CreateShader);
    AU_IMPL_API_REGISTER(DeleteShader);
    AU_IMPL_API_REGISTER(ShaderSource);
    AU_IMPL_API_REGISTER(CompileShader);

    AU_IMPL_API_REGISTER(CreateProgram);
    AU_IMPL_API_REGISTER(DeleteProgram);
    AU_IMPL_API_REGISTER(UseProgram);
    AU_IMPL_API_REGISTER(AttachShader);
    AU_IMPL_API_REGISTER(DetachShader);
    AU_IMPL_API_REGISTER(LinkProgram);
    AU_IMPL_API_REGISTER(GetUniformLocation);
    AU_IMPL_API_REGISTER(ProgramUniformMatrix4fv);

    AU_IMPL_API_REGISTER(CreateTextures);
    AU_IMPL_API_REGISTER(DeleteTextures);
    AU_IMPL_API_REGISTER(TextureStorage2D);

    AU_IMPL_API_REGISTER(CreateRenderbuffers);
    AU_IMPL_API_REGISTER(DeleteRenderbuffers);
    AU_IMPL_API_REGISTER(NamedRenderbufferStorage);

    AU_IMPL_API_REGISTER(CreateFramebuffers);
    AU_IMPL_API_REGISTER(DeleteFramebuffers);
    AU_IMPL_API_REGISTER(BindFramebuffer);
    AU_IMPL_API_REGISTER(NamedFramebufferTexture);
    AU_IMPL_API_REGISTER(NamedFramebufferRenderbuffer);
    AU_IMPL_API_REGISTER(ClearNamedFramebufferfv);

    AU_IMPL_API_REGISTER(Viewport);
    AU_IMPL_API_REGISTER(DrawArrays);
    AU_IMPL_API_REGISTER(DrawElements);
}

#define AU_IMPL_GL_EXPAND(L, name) lua_pushinteger(L, GL_ ## name); lua_setfield(L, -2, #name)
void aurora::api_register_gl_constants(lua_State *L) {
    lua_newtable(L);
    AU_IMPL_GL_EXPAND(L, COLOR);
    AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT0);
    AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT1);
    AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT2);
    AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT3);
    AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT4);
    AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT5);
    AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT6);
    AU_IMPL_GL_EXPAND(L, COLOR_ATTACHMENT7);
    AU_IMPL_GL_EXPAND(L, DEPTH);
    AU_IMPL_GL_EXPAND(L, DEPTH_ATTACHMENT);
    AU_IMPL_GL_EXPAND(L, DEPTH_COMPONENT24);
    AU_IMPL_GL_EXPAND(L, DEPTH_COMPONENT32);
    AU_IMPL_GL_EXPAND(L, DEPTH_COMPONENT32F);
    AU_IMPL_GL_EXPAND(L, FLOAT);
    AU_IMPL_GL_EXPAND(L, FRAGMENT_SHADER);
    AU_IMPL_GL_EXPAND(L, FRAMEBUFFER);
    AU_IMPL_GL_EXPAND(L, NONE);
    AU_IMPL_GL_EXPAND(L, RENDERBUFFER);
    AU_IMPL_GL_EXPAND(L, RGBA8);
    AU_IMPL_GL_EXPAND(L, STENCIL);
    AU_IMPL_GL_EXPAND(L, TEXTURE_2D);
    AU_IMPL_GL_EXPAND(L, TRIANGLES);
    AU_IMPL_GL_EXPAND(L, TRIANGLE_STRIP);
    AU_IMPL_GL_EXPAND(L, UNSIGNED_INT);
    AU_IMPL_GL_EXPAND(L, UNSIGNED_SHORT);
    AU_IMPL_GL_EXPAND(L, VERTEX_SHADER);
}
#undef AU_IMPL_GL_EXPAND