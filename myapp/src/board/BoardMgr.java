package board;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.tomcat.jni.OS;
import member.DBConnectionMgr;
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

public class BoardMgr {
	private DBConnectionMgr pool;
	private static final String SAVEFOLDER = "C:/Git_project/JspWebPracticeProject/JspWebPracticeProject/myapp/WebContent/board/fileupload";
	private static final String ENCTYPE = "UTF-8";
	private static int MAXSIZE = 5 * 1024 * 1024;
	
	public BoardMgr() {
		try {
			pool = DBConnectionMgr.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 게시판 리스트.
	public Vector<BoardBean> getBoardList(String keyField, String keyword, int start, int end){
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		Vector<BoardBean> vlist = new Vector<BoardBean>();
		try {
			conn = pool.getConnection();
			if (keyword.equals("null") || keyword.equals("")) {
				sql = "SELECT * FROM tblBoard ORDER BY ref DESC, pos LIMIT ?, ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, start);
				pstmt.setInt(2, end);
			}else {
				sql = "SELECT * FROM tblBoard WHERE " + keyField + " LIKE ? ORDER BY ref DESC, pos LIMIT ?, ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "%" + keyword + "%");
				pstmt.setInt(2, start);
				pstmt.setInt(3, end);
			}
			rs = pstmt.executeQuery();
			while (rs.next()) {
				BoardBean bean = new BoardBean();
				bean.setNum(rs.getInt("num"));
				bean.setName(rs.getString("name"));
				bean.setSubject(rs.getString("subject"));
				bean.setPos(rs.getInt("pos"));
				bean.setRef(rs.getInt("ref"));
				bean.setDepth(rs.getInt("depth"));
				bean.setRegdate(rs.getString("regdate"));
				bean.setCount(rs.getInt("count"));
				vlist.add(bean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		return vlist;
	}
	
	// 총 게시물 수.
	public int getTotalCount(String keyField, String keyword) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		int totalCount = 0;
		try {
			conn = pool.getConnection();
			if (keyword.equals("null") || keyword.equals("")) {
				sql = "SELECT COUNT(num) FROM tblBoard";
				pstmt = conn.prepareStatement(sql);
			}else {
				sql = "SELECT COUNT(num) FROM tblBoard WHERE " + keyField + " LIKE ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "%" + keyword + "%");
			}
			rs = pstmt.executeQuery();
			if (rs.next()) {
				totalCount = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		return totalCount;
	}
	
	// 게시물 입력.
	public void insertBoard(HttpServletRequest request) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		MultipartRequest multi = null;
		int filesize = 0;
		String filename = null;
		try {
			conn = pool.getConnection();
			sql = "SELECT MAX(num) FROM tblBoard";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			int ref = 1;
			if (rs.next()) {
				ref = rs.getInt(1) + 1;
			}
			multi = new MultipartRequest(request, SAVEFOLDER, MAXSIZE, ENCTYPE, new DefaultFileRenamePolicy());
			if (multi.getFilesystemName("filename") != null) {
				filename = multi.getFilesystemName("filename");
				filesize = (int)multi.getFile("filename").length();
			}
			String content = multi.getParameter("content");
			if (multi.getParameter("contentType").equalsIgnoreCase("TEXT")) {
				content = UtilMgr.replace(content, "<", "&lt;");
			}
			sql = "INSERT INTO tblBoard(name,content,subject,ref,pos,depth,regdate,pass,count,ip,filename,filesize) VALUES(?, ?, ?, ?, 0, 0, now(), ?, 0, ?, ?, ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, multi.getParameter("name"));
			pstmt.setString(2, content);
			pstmt.setString(3, multi.getParameter("subject"));
			pstmt.setInt(4, ref);
			pstmt.setString(5, multi.getParameter("pass"));
			pstmt.setString(6, multi.getParameter("ip"));
			pstmt.setString(7, filename);
			pstmt.setInt(8, filesize);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
	}
	
	// 게시물 리턴.
	public BoardBean getBoard(int num) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		BoardBean bean = new BoardBean();
		try {
			conn = pool.getConnection();
			sql = "SELECT * FROM tblBoard WHERE num = ?";
			pstmt= conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				bean.setNum(rs.getInt("num"));
				bean.setName(rs.getString("name"));
				bean.setSubject(rs.getString("subject"));
				bean.setContent(rs.getString("content"));
				bean.setPos(rs.getInt("pos"));
				bean.setRef(rs.getInt("ref"));
				bean.setDepth(rs.getInt("depth"));
				bean.setRegdate(rs.getString("regdate"));
				bean.setPass(rs.getString("pass"));
				bean.setCount(rs.getInt("count"));
				bean.setFilename(rs.getString("filename"));
				bean.setFilesize(rs.getInt("filesize"));
				bean.setIp(rs.getString("ip"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		return bean;
	}
	
	// 조회수 증가.
	public void upCount(int num) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "";
		try {
			conn = pool.getConnection();
			sql = "UPDATE tblBoard SET count = count + 1 WHERE num = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
	}
	
	// 게시물 삭제.
	public void deleteBoard(int num) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "";
		ResultSet rs = null;
		try {
			conn = pool.getConnection();
			sql = "SELECT filename FROM tblBoard WHERE num = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if (rs.next() && rs.getString(1) != null) {
				if (!rs.getString(1).equals("")) {
					File file = new File(SAVEFOLDER + "/" + rs.getString(1));
					if (file.exists()) {
						UtilMgr.delete(SAVEFOLDER + "/" + rs.getString(1));
					}
				}
			}
			sql = "DELETE FROM tblBoard WHERE num = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
	}
	
	// 게시물 수정.
	public void updateBoard(BoardBean bean) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "";
		try {
			conn = pool.getConnection();
			sql = "UPDATE tblBoard SET name = ?, subject = ?, content = ? WHERE num = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, bean.getName());
			pstmt.setString(2, bean.getSubject());
			pstmt.setString(3, bean.getContent());
			pstmt.setInt(4, bean.getNum());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
	}
	
	// 게시물 답변.
	public void replyBoard(BoardBean bean) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "";
		try {
			conn = pool.getConnection();
			sql = "INSERT tblBoard (name,content,subject,ref,pos,depth,regdate,pass,count,ip)";
			sql += "VALUES(?,?,?,?,?,?,now(),?,0,?)";
			int depth = bean.getDepth() + 1;
			int pos = bean.getPos() + 1;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, bean.getName());
			pstmt.setString(2, bean.getContent());
			pstmt.setString(3, bean.getSubject());
			pstmt.setInt(4, bean.getRef());
			pstmt.setInt(5, pos);
			pstmt.setInt(6, depth);
			pstmt.setString(7, bean.getPass());
			pstmt.setString(8, bean.getIp());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
	}
	
	// 답변의 위치값 증가.
	public void replyUpBoard(int ref, int pos) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "";
		try {
			conn = pool.getConnection();
			sql = "UPDATE tblBoard SET pos = pos + 1 WHERE ref = ? AND pos > ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, ref);
			pstmt.setInt(2, pos);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
	}
	
	// 파일 다운로드.
	public void downLoad(HttpServletRequest request, HttpServletResponse response, JspWriter out, PageContext pageContext) {
		try {
			String filename = request.getParameter("filename");
			File file = new File(UtilMgr.con(SAVEFOLDER + File.separator+ filename));
			byte b[] = new byte[(int) file.length()];
			response.setHeader("Accept-Ranges", "bytes");
			String strClient = request.getHeader("User-Agent");
			if (strClient.indexOf("MSIE6.0") != -1) {
				response.setContentType("application/smnet;charset=utf-8");
				response.setHeader("Content-Disposition", "filename=" + filename + ";");
			} else {
				response.setContentType("application/smnet;charset=utf-8");
				response.setHeader("Content-Disposition", "attachment;filename="+ filename + ";");
			}
			out.clear();
			out = pageContext.pushBody();
			if (file.isFile()) {
				BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
				BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
				int read = 0;
				while ((read = fin.read(b)) != -1) {
					outs.write(b, 0, read);
				}
				outs.close();
				fin.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 페이징 및 블럭 테스트를 위한 게시물 저장.
	public void post1000() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			conn = pool.getConnection();
			sql = "INSERT INTO tblBoard(name,content,subject,ref,pos,depth,regdate,pass,count,ip,filename,filesize)";
			sql += "VALUES('aaa', 'bbb', 'ccc', 0, 0, 0, now(), '1111',0, '127.0.0.1', null, 0);";
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < 1000; i++) {
				pstmt.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
	}
	
	// 메인 메서드.
	public static void main(String[] args) {
		new BoardMgr().post1000();
		System.out.println("성공");
	}
}
