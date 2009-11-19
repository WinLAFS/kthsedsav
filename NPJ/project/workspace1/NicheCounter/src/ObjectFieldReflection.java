import java.lang.reflect.Field;
import java.util.ArrayList;

public class ObjectFieldReflection {
  public static void main(String args[]) throws Exception {
    printAllFields(new ArrayList());
  }
  
  public static void printAllFields(Object obj) throws IllegalArgumentException, IllegalAccessException{
	  Field[] f_a = obj.getClass().getDeclaredFields();
	    for (Field f : f_a) {
	      f.setAccessible(true);
	      System.out.println("Fieldname="+f.getName()+" value="+f.get(obj));
	    }
  }
}