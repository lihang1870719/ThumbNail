package com.lihang.pti;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class DBConn {

	static {
		try {
			Class.forName("org.gjt.mm.mysql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static DBConn make(Config config) throws SQLException {
		return new DBConn(config);
	}

	private final Config config;
	private final Connection conn;

	private DBConn(Config config) throws SQLException {
		this.config = config;
		String connString = String.format("jdbc:mysql://%s/%s",
				config.get("connection.host"),
				config.get("connection.database"));
		conn = DriverManager.getConnection(connString,
				config.get("connection.user"),
				config.get("connection.password"));
	}

	public void close() {
		try {
			this.conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Integer> getDocIds() throws SQLException {
		String sql = "select id from docs "
				+ "where status = 'uploaded' and format = 'pdf' ";
		if (!config.getBool("dev")) {
			sql += "and thumbnail is null ";
		}
		sql += "and deleted_at is null " + "order by created_at desc";
		ResultSet rs = conn.createStatement().executeQuery(sql);
		List<Integer> result = new ArrayList<Integer>();
		while (rs.next()) {
			result.add(rs.getInt(1));
		}
		return result;
	}

	public void updateThumbnail(int id, String url) throws Exception {
		String updateString = "update docs set thumbnail = ?" + " WHERE id = ?";
		PreparedStatement ps = conn.prepareStatement(updateString);
		ps.setString(1, url);
		ps.setInt(2, id);
		ps.executeUpdate();
	}

}
