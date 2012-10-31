
package jns.trace;

// A utility class to bundle a parameter to an event: A name and a value.

public class EventParameter
{

    public String name;
    public Object value;

    public EventParameter()
    {
        name = new String();
        value = null;
    }

    public EventParameter(String name, Object value)
    {
        this.name = name;
        this.value = value;
    }

    public boolean equals(EventParameter other)
    {
        return name.equals(other.name) && value.equals(other.value);
    }
}

