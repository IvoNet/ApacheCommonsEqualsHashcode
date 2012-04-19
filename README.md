# Introduction #

An IntelliJ Plugin for generating equals and hashCode methods based on the commons-lang
EqualsBuilder and HashCodeBuilder Builders.

This little plugin is adaptation of the equals and hasCode generator [deluxe][]

[deluxe]: https://github.com/mjedynak/EqualsHashCodeDeluxeGenerator

It has been tested on IntelliJ 11

# Example #

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(this.foo).append(this.bar)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Foo other = (Foo) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(this.foo, other.foo)
                                  .append(this.bar, other.bar).isEquals();
    }