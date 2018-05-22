<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>

<table style="width: 100%;">
	<tbody>
		<tr>
			<td>
				<div style="float: left;">
					<fieldset>
						<legend>Device Name</legend>
						<select id="DeviceNameOption" onclick="getDeviceInfo();" size="20" style="width: 180px;">
							<c:forEach items="${DeviceNames}" var="devices">
								<option value="${devices.key}" style="cursor: pointer; margin-top: 2px;">${devices.value }</option>
							</c:forEach>
						</select>
					</fieldset>
				</div>
				<div style="float: right; width: 78%;">
					<fieldset>
						<legend>Device Info</legend>
						<table>
							<tbody>
								<tr>
									<td><label>Device Name</label></td>
									<td><input type="text" id="deviceName" style="margin-left: 5px;"></td>
									<td rowspan="2"><label style="margin-left: 5px; margin-top: -43px;">Memory Mapping</label></td>
									<td rowspan="2"><textarea id="memoryMapping" rows="4" cols="10" style="margin-left: 5px;"></textarea></td>
								</tr>
								<tr>
									<td><label>Unit Id</label></td>
									<td><input type="text" id="unitId" style="margin-left: 5px;"></td>
								</tr>
								<tr>
									<td><label>Baud Rate</label></td>
									<td>
										<select id="baudRate" style="margin-left: 5px;">
											<option value="0">--select--</option>
											<c:forEach items="${BaudRates}" var="devices">
												<option value="${devices}" style="cursor: pointer; margin-top: 2px;">${devices}</option>
											</c:forEach>
										</select>
									</td>
									<td><label>Word Length</label></td>
									<td>
										<select id="wordLength" style="margin-left: 5px;">
											<option value="0">--select--</option>
											<c:forEach items="${WordLength}" var="devices">
												<option value="${devices}" style="cursor: pointer; margin-top: 2px;">${devices}</option>
											</c:forEach>
										</select>
									</td>
								</tr>
								<tr>
									<td><label>Parity</label></td>
									<td>
										<select id="parity" style="margin-left: 5px;">
											<option value="0">--select--</option>
											<c:forEach items="${Parity}" var="devices">
												<option value="${devices}" style="cursor: pointer; margin-top: 2px;">${devices}</option>
											</c:forEach>
										</select>
									</td>
									<td><label>Stop Bit</label></td>
									<td>
										<select id="stopbit" style="margin-left: 5px;">
											<option value="0">--select--</option>
											<c:forEach items="${StopBit}" var="devices">
												<option value="${devices}" style="cursor: pointer; margin-top: 2px;">${devices}</option>
											</c:forEach>
										</select>
									</td>
								</tr>
								<tr>
									<td><label>Reg Mapping</label></td>
									<td>
										<select id="regmapping" style="margin-left: 5px;">
											<option value="0">--select--</option>
											<c:forEach items="${RegMapping}" var="devices">
												<option value="${devices}" style="cursor: pointer; margin-top: 2px;">${devices}</option>
											</c:forEach>
										</select>
									</td>
									<td><label>Port</label></td>
									<td>
										<select id="port" style="margin-left: 5px;">
											<option value="0">--select--</option>
											<c:forEach items="${Port}" var="devices">
												<option value="${devices}" style="cursor: pointer; margin-top: 2px;">${devices}</option>
											</c:forEach>
										</select>
									</td>
								</tr>
								<tr>
									<td><label>Method</label></td>
									<td>
										<select id="method" style="margin-left: 5px;">
											<option value="0">--select--</option>
											<c:forEach items="${Method}" var="devices">
												<option value="${devices}" style="cursor: pointer; margin-top: 2px;">${devices}</option>
											</c:forEach>
										</select>
									</td>
									<td><label>Enable</label></td>
									<td>
										<span>
											<span style="float: left; margin-left: 5px;"><input type="radio" id="enabledRadioId" name="enabledRadio" value="true"><label for="enabledRadioId" style="cursor: pointer;">Enabled</label></span>
											<span style="float: right; margin-right: 80px;"><input type="radio" id="disabledRadioId" name="enabledRadio" value="false"><label for="disabledRadioId" style="cursor: pointer;">Disabled</label></span>
										</span>
									</td>
								</tr>
								<tr>
									<td colspan="4">
										<input type="hidden" id="deviceUniqueId">
										<span style="float: right; margin-top: 20px;">
											<button onclick="removeDevice();" style="float: left; margin-right: 15px; border-radius: 5px;">Remove Device</button>
											<button onclick="updateChanges();" style="float: right; margin-right: 30px; border-radius: 5px;">Update Changes</button>
										</span>
									</td>
								</tr>
							</tbody>
						</table>
					</fieldset>
				</div>
			</td>
		</tr>
	</tbody>
</table>