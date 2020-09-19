package board;

import java.io.File;

// �Խ��� ��ƿ ���� �ڹ� ����.
public class UtilMgr {
	
	// str = ����, pattern = �������� ã�� ����, replace = �ٲ� ����.
	public static String replace(String str, String pattern, String replace) { // 3���� ���ڿ� Ÿ���� �Ű������� �޴´�.
		int s = 0, e = 0;
		StringBuffer result = new StringBuffer();
		// ù��° �Ű����� str�� ���ڿ� �� �ι�° �Ű����� pattern�� �ش�Ǵ� �κ��� ã�Ƽ�, ����° �Ű����� replace�� �ٲٴ� ����.
		while ((e = str.indexOf(pattern, s)) >= 0) { 
			result.append(str.substring(s, e)); 
			result.append(replace);
			s = e + pattern.length();
		}
		result.append(str.substring(s));
		return result.toString();
	}
	
	// ����ڰ� �Խù��� ������ �� ������ �Խù��� ÷�������� ���� ���, �Ű������� �Ѿ�� ���ϸ� �ش��ϴ� ������ ����.
	public static void delete(String s) {
		File file = new File(s);
		if (file.isFile()) {
			file.delete();
		}
	}
	
	// ÷�������� �ٿ�ε��� ��, ��γ� ���ϸ� ���� �ѱ��� ������ �ʵ��� ���ڵ� ����� ��ȯ.
	public static String con(String s) {
		String str = null;
		try {
			str = new String(s.getBytes("8859_1"), "ksc5601");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}
}
