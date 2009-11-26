import java.lang.reflect.Field;
import java.util.ArrayList;

public class ObjectFieldReflection {
  public static void main(String args[]) throws Exception {
    printAllFields(new ArrayList());
  }
  
  public static void printAllFields(Object obj) throws IllegalArgumentException, IllegalAccessException{
	  int v=0;
	    for (int i=0; i<12; i++) {
	      System.out.println(v++);

	    }
  }
}