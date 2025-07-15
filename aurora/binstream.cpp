// This implementation is still experimental and not final, more development must be done before this is ready to use

#include <string>
#include <vector>
#include <cstdint>

struct Serializer;

struct Serializable {
	constexpr Serializable() noexcept = default;
	~Serializable() noexcept = default;
	Serializable(Serializable const&) = delete;
	Serializable& operator=(Serializable const&) = delete;
	Serializable(Serializable&&) noexcept = delete;
	Serializable& operator=(Serializable&&) noexcept = delete;
	virtual void serialize(Serializer& a) = 0;
};

struct Serializer {
	constexpr Serializer() noexcept = default;
	virtual ~Serializer() = default;
	Serializer(Serializer const&) = delete;
	Serializer& operator=(Serializer const&) = delete;
	Serializer(Serializer&&) noexcept = delete;
	Serializer& operator=(Serializer&&) noexcept = delete;

	void serialize(char const* aField, Serializable& aValue) { aValue.serialize(*this); }
	virtual void serialize(char const* aField, bool& aValue) = 0;
	virtual void serialize(char const* aField, float& aValue) = 0;
	virtual void serialize(char const* aField, std::uint8_t& aValue) = 0;
	virtual void serialize(char const* aField, std::uint16_t& aValue) = 0;
	virtual void serialize(char const* aField, std::uint32_t& aValue) = 0;
	virtual void serialize(char const* aField, std::int8_t& aValue) = 0;
	virtual void serialize(char const* aField, std::int16_t& aValue) = 0;
	virtual void serialize(char const* aField, std::int32_t& aValue) = 0;
	virtual void serialize(char const* aField, std::string& aValue, bool aSizedString = true) = 0;

	/// not final
	template<typename T> void serialize(char const* aField, std::vector<T>& aValue) {
		for (auto& element : aValue) {
			serialize(nullptr, element);
		}
	}
};

#define AU_FIELD(aSerializer, aField) aSerializer.serialize(#aField, aField)
