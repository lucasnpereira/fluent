package examples;

import org.fluentj.core.annotations.GenerateExtensibleFluentClass;

/**
 * Created by lucas on 13/02/17.
 */
@GenerateExtensibleFluentClass
public class MyFluent {

    public MyFluent doStuff() {
        return this;
    }

    public MyFluent doSomeOtherStuff() { return this;}
}
