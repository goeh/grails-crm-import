package test

/**
 * Test entity.
 */
class TestEntity {
    String number
    String name
    String address
    String postalCode
    String city
    String phone
    String email
    String source

    static constraints = {
        number(maxSize: 20, blank: false)
        name(maxSize: 80, blank: false)
        address(nullable: true)
        postalCode(maxSize: 20, blank: false)
        city(maxSize: 40, blank: false)
        phone(maxSize: 20, nullable: true)
        email(maxSize: 255, nullable: true)
        source(maxSize:40, nullable: true)
    }

    String toString() {
        name.toString()
    }
}
