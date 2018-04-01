import com.ems.security.Security;

public class Main {

	
	public static void main(String[] args) throws Exception{
		
		System.out.println(Security.getInstance().encrypt("123123"));
		
	}
}
