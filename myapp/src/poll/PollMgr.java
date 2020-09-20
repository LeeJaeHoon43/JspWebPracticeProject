package poll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import member.DBConnectionMgr;

public class PollMgr {
	private DBConnectionMgr pool;
	
	public PollMgr() {
		try {
			pool = DBConnectionMgr.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getMaxNum() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		int maxNum = 0;
		try {
			conn = pool.getConnection();
			sql = "SELECT MAX(num) FROM tblPollList";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				maxNum = rs.getInt(1); // 가장 높은 num값.
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		return maxNum;
	}
	
	public boolean insertPoll(PollListBean plBean, PollItemBean piBean) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean flag = false;
		String sql = "";
		try {
			conn = pool.getConnection();
			sql = "INSERT tblPollList(question, sdate, edate, wdate, type) VALUES(?,?,?,now(),?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, plBean.getQuestion());
			pstmt.setString(2, plBean.getSdate());
			pstmt.setString(3, plBean.getEdate());
			pstmt.setInt(4, plBean.getType());
			int result = pstmt.executeUpdate();
			if (result == 1) {
				sql = "INSERT INTO tblPollItem VALUES(?,?,?,?)";
				pstmt = conn.prepareStatement(sql);
				String item[] = piBean.getItem();
				int itemnum = getMaxNum();
				int j = 0;
				for (int i = 0; i < item.length; i++) {
					if (item[i] == null || item[i].equals("")) {
						break;
					}
					pstmt.setInt(1, itemnum);
					pstmt.setInt(2, i);
					pstmt.setString(3, item[i]);
					pstmt.setInt(4, 0);
					j = pstmt.executeUpdate();
				}
				if (j > 0) {
					flag = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
		return flag;
	}
	
	public Vector<PollListBean> getAllList(){
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		Vector<PollListBean> vlist = new Vector<PollListBean>();
		try {
			conn = pool.getConnection();
			sql = "SELECT * FROM tblPollList ORDER BY num DESC";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				PollListBean plBean = new PollListBean();
				plBean.setNum(rs.getInt("num"));
				plBean.setQuestion(rs.getString("question"));
				plBean.setSdate(rs.getString("sdate"));
				plBean.setEdate(rs.getString("edate"));
				vlist.add(plBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		return vlist;
	}
	
	public PollListBean getList(int num) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		PollListBean plBean = new PollListBean();
		try {
			conn = pool.getConnection();
			if (num == 0) {
				sql = "SELECT * FROM tblPollList ORDER BY num DESC";
			}else {
				sql = "SELECT * FROM tblPollList WHERE num = " + num;
			}
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				plBean.setQuestion(rs.getString("question"));
				plBean.setType(rs.getInt("type"));
				plBean.setActive(rs.getInt("active"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		return plBean;
	}
	
	public Vector<String>getItem(int num){
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		Vector<String> vlist = new Vector<String>();
		try {
			conn = pool.getConnection();
			if (num == 0) {
				num = getMaxNum();
			}
			sql = "SELECT item FROM tblPollItem WHERE listnum = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				vlist.add(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		return vlist;
	}
	
	public boolean updatePoll(int num, String[] itemnum) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean flag = false;
		String sql = "";
		try {
			conn = pool.getConnection();
			sql = "UPDATE tblPollItem SET count = count + 1 WHERE listnum = ? AND itemnum = ?";
			pstmt = conn.prepareStatement(sql);
			if (num == 0) {
				num = getMaxNum();
			}
			for (int i = 0; i < itemnum.length; i++) {
				if (itemnum[i] == null || itemnum[i].equals("")) {
					break;
				}
				pstmt.setInt(1, num);
				pstmt.setInt(2, Integer.parseInt(itemnum[i]));
				int j = pstmt.executeUpdate();
				if (j > 0) {
					flag = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		} 
		return flag;
	}
	
	public Vector<PollItemBean> getView(int num){
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		Vector<PollItemBean> vlist = new Vector<PollItemBean>();
		try {
			conn = pool.getConnection();
			sql = "SELECT item, count FROM tblPollItem WHERE listnum = ?";
			pstmt = conn.prepareStatement(sql);
			if (num == 0) {
				pstmt.setInt(1, getMaxNum());
			}else {
				pstmt.setInt(1, num);
			}
			rs = pstmt.executeQuery();
			while (rs.next()) {
				PollItemBean piBean= new PollItemBean();
				String item[] = new String[1];
				item[0] = rs.getString(1);
				piBean.setItem(item);
				piBean.setCount(rs.getInt(2));
				vlist.add(piBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		return vlist;
	}
	
	public int sumCount(int num) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		int count = 0;
		try {
			conn = pool.getConnection();
			sql = "SELECT SUM(count) FROM tblPollItem WHERE listnum = ?";
			pstmt = conn.prepareStatement(sql);
			if (num == 0) {
				pstmt.setInt(1, getMaxNum());				
			}else {				
				pstmt.setInt(1, num);
			}
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
		return count;
	}
}
