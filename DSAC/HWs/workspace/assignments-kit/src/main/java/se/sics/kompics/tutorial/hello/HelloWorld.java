package se.sics.kompics.tutorial.hello;

import se.sics.kompics.PortType;

public class HelloWorld extends PortType {
	{
		positive(World.class);
		negative(Hello.class);
	}
}
