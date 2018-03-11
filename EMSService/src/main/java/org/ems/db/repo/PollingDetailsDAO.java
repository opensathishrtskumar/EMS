package org.ems.db.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.ems.reports.summary.finder.SummaryFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.UI.dto.SettingsDTO;
import com.ems.constants.QueryConstants;
import com.ems.util.EMSUtility;

@Repository("pollingDetailsDAO")
public class PollingDetailsDAO {

	private static final Logger logger = LoggerFactory.getLogger(PollingDetailsDAO.class);

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public List<DeviceDetailsDTO> fetchAllDeviceDetails(String query, Object[] params) {

		return this.jdbcTemplate.query(query, new RowMapper<DeviceDetailsDTO>() {

			@Override
			public DeviceDetailsDTO mapRow(ResultSet resultSet, int rowIndex) throws SQLException {
				DeviceDetailsDTO details = new DeviceDetailsDTO();

				details.setRowIndex(rowIndex++);
				details.setUniqueId(resultSet.getLong("deviceuniqueid"));
				details.setDeviceId(resultSet.getInt("unitid"));
				details.setDeviceName(resultSet.getString("devicealiasname"));
				details.setBaudRate(resultSet.getInt("baudrate"));
				details.setWordLength(resultSet.getInt("wordlength"));
				details.setStopbit(resultSet.getInt("stopbit"));
				details.setParity(resultSet.getString("parity"));
				details.setMemoryMapping(resultSet.getString("memorymapping"));// register_assignment
				boolean splitJoin = EMSUtility.isSplitJoin(details.getMemoryMapping());
				details.setSplitJoin(splitJoin);
				details.setEnabled(resultSet.getBoolean("status") ? "true" : "false");
				details.setRegisterMapping(resultSet.getString("registermapping"));// MSRF/LSRF
				details.setPort(resultSet.getString("port"));
				details.setMethod(resultSet.getString("method"));

				return details;
			}
		}, params);
	}

	public int executeQuery(final String query, final Object[] params) {

		return this.jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(query);
				logger.trace("StmtCreator for query : {} with params : {}", query, Arrays.toString(params));
				if (params != null) {
					int count = 1;
					for (Object param : params) {
						ps.setObject(count++, param);
					}
				}

				return ps;
			}
		});
	}

	public void execute(final String query) {
		jdbcTemplate.execute(query);
	}

	public int execute(final String query, Object[] params) {
		jdbcTemplate.execute(query,new PreparedStatementCallback<Object>() {
			@Override
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				if (params != null) {
					int count = 1;
					for (Object param : params) {
						ps.setObject(count++, param);
					}
				}

				return ps;
			}
		});
		return 0;
	}


	public List<SettingsDTO> fetchSettings() {

		return this.jdbcTemplate.query(QueryConstants.FETCH_SETTINGS, new RowMapper<SettingsDTO>() {
			@Override
			public SettingsDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
				SettingsDTO dto = new SettingsDTO();
				dto.setId(rs.getLong("id"));
				dto.setSkey(rs.getString("skey"));
				dto.setSvalue(rs.getString("svalue"));
								
				return dto;
			}
					
		});
	}

	public String fetchVariable(String query){
		return this.jdbcTemplate.queryForObject(query, String.class);
	}

	public List<PollingDetailDTO> fetchMainIncomerDailySummary(String query, Object[] params) {
		
		logger.trace(" entry ");
		
		List<PollingDetailDTO> details = this.jdbcTemplate.query(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				con.setAutoCommit(false);
				PreparedStatement statement = con.prepareStatement(query);
				
				int index = 1;
				for(Object obj : params) statement.setObject(index++, obj);
				
				statement.setFetchSize(Integer.MIN_VALUE);
				logger.debug(" prepared statement created {} ", Arrays.toString(params));
				return statement;
			}
		}, new RowMapper<PollingDetailDTO>() {

			@Override
			public PollingDetailDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
				PollingDetailDTO details = new PollingDetailDTO();
				details.setDeviceuniqueid(rs.getLong("deviceuniqueid"));
				details.setPolledon(rs.getLong("polledon"));
				details.setUnitresponse(rs.getString("unitresponse"));
					
			
			return details;
			}
		});

		logger.trace(" exit " + EMSUtility.convertObjectToJSONString(details));
		
		return details;
	}
	
	
	public SummaryFinder calculateSummary(String query, Object[] params, SummaryFinder summary ){
		
		logger.debug(" entry ");
		
		this.jdbcTemplate.query(new PreparedStatementCreator() {
			
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				con.setAutoCommit(false);
				PreparedStatement ps = con.prepareStatement(query,
						ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				int index = 1;
				for(int i = 0;i < params.length;i++)
					ps.setObject(index++, params[i]);
				ps.setFetchSize(Integer.MIN_VALUE);
				
				logger.trace(" prepared stmt created for {} ", query);
				return ps;
			}
		}, new RowCallbackHandler() {
			
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				try {
					summary.process(rs);
				} catch (Exception e) {
					logger.error("{}" , e);
				}
			}
		});
		
		logger.debug(" exit ");
		
		return summary;
	}
}