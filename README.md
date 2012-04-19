# Introduction #

This little plugin is based on the equals and hasCode generator deluxe
(https://github.com/mjedynak/EqualsHashCodeDeluxeGenerator).

This plugin generates equals and hashCode methods based on the commons-lang
EqualsBuilder and HashCodeBuilder classes.

# Example #

[code]
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Foo {
    private String foo;
    private String bar;

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
}
[code]