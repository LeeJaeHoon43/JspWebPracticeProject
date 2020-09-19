package board;

import java.io.File;

// 게시판 유틸 관련 자바 파일.
public class UtilMgr {
	
	// str = 문서, pattern = 문서에서 찾을 내용, replace = 바꿀 내용.
	public static String replace(String str, String pattern, String replace) { // 3개의 문자열 타입의 매개변수를 받는다.
		int s = 0, e = 0;
		StringBuffer result = new StringBuffer();
		// 첫번째 매개변수 str의 문자열 중 두번째 매개변수 pattern에 해당되는 부분을 찾아서, 세번째 매개변수 replace로 바꾸는 역할.
		while ((e = str.indexOf(pattern, s)) >= 0) { 
			result.append(str.substring(s, e)); 
			result.append(replace);
			s = e + pattern.length();
		}
		result.append(str.substring(s));
		return result.toString();
	}
	
	// 사용자가 게시물을 삭제할 때 삭제할 게시물에 첨부파일이 있을 경우, 매개변수로 넘어온 파일명에 해당하는 파일을 삭제.
	public static void delete(String s) {
		File file = new File(s);
		if (file.isFile()) {
			file.delete();
		}
	}
	
	// 첨부파일을 다운로드할 때, 경로나 파일명에 대한 한글이 깨지지 않도록 인코딩 방식을 변환.
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
