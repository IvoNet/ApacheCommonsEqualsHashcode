# Introduction #

An IntelliJ Plugin for generating equals and hashCode methods based on the commons-lang
EqualsBuilder and HashCodeBuilder Builders.

See [here](http://www.ivonet.nl/home/blog/t/1001) for mor information.

This little plugin is loosly based of the equals and hasCode generator [deluxe](https://github.com/mjedynak/EqualsHashCodeDeluxeGenerator)

It has been tested on IntelliJ 11 and 12
On Windows and Mac Operating Systems

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

# Note:

This project was cool for intellij but is now deprecated because its more or less a standard feature in this fantastic IDE.
