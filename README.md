# fluent
A library for creating fluent interfaces in Java.

Annotate a class with @GenerateExtensibleFluentClass and the library will generate a subclass with a recursive generic signature to allow for easy extensions.

Example input:

@GenerateExtensibleFluentClass
public class MyFluent {

    public MyFluent doStuff() {
        return this;
    }

    public MyFluent doSomeOtherStuff() { return this;}
}

Example output:

@SuppressWarnings("all")
class ExtensibleMyFluent<Self extends MyFluent> extends MyFluent {
  @Override
  public Self doStuff() {
    super.doStuff();
    return (Self) this;
  }

  @Override
  public Self doSomeOtherStuff() {
    super.doSomeOtherStuff();
    return (Self) this;
  }
}
