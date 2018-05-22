<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>

<h2>Add Device</h2>
<fieldset>
	<legend>Device Detail</legend>
	<table>
		<tbody>
			<tr>
				<td><label>Device Name</label></td>
				<td><input name="deviceName" type="text" id="deviceName" style="margin-left: 5px;"></td>
				<td rowspan="2"><label style="margin-left: 5px; margin-top: -43px;">Memory Mapping</label></td>
				<td rowspan="2"><textarea id="memoryMapping" name="memoryMapping" rows="4" cols="10" style="margin-left: 5px;"></textarea></td>
			</tr>
			<tr>
				<td><label>Unit Id</label></td>
				<td><input name="unitId" type="text" id="unitId" style="margin-left: 5px;"></td>
			</tr>
			<tr>
				<td><label>Baud Rate</label></td>
				<td>
					<select id="baudRate" name="baudRates" style="margin-left: 5px;">
						<option value="0">--select--</option>
						<c:forEach items="${BaudRates}" var="devices">
							<option value="${devices}" style="cursor: pointer; margin-top: 2px;">${devices}</option>
						</c:forEach>
					</select>
				</td>
				<td><label>Word Length</label></td>
				<td>
					<select id="wordLength" name="wordLength" style="margin-left: 5px;">
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
					<select id="parity" name="parity" style="margin-left: 5px;">
						<option value="0">--select--</option>
						<c:forEach items="${Parity}" var="devices">
							<option value="${devices}" style="cursor: pointer; margin-top: 2px;">${devices}</option>
						</c:forEach>
					</select>
				</td>
				<td><label>Stop Bit</label></td>
				<td>
					<select id="stopbit" name="stopBit" style="margin-left: 5px;">
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
					<select id="regmapping" name="regMapping" style="margin-left: 5px;">
						<option value="0">--select--</option>
						<c:forEach items="${RegMapping}" var="devices">
							<option value="${devices}" style="cursor: pointer; margin-top: 2px;">${devices}</option>
						</c:forEach>
					</select>
				</td>
				<td><label>Port</label></td>
				<td>
					<select id="port" name="port" style="margin-left: 5px;">
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
					<select id="method" name="readMethod" style="margin-left: 5px;">
						<option value="0">--select--</option>
						<c:forEach items="${Method}" var="devices">
							<option value="${devices}" style="cursor: pointer; margin-top: 2px;">${devices}</option>
						</c:forEach>
					</select>
				</td>
				<td><label>Enable</label></td>
				<td>
					<span style="float: left; margin-top: 20px;">
						<span style="float: left; margin-left: 5px;"><input type="radio" id="enabledRadio" name="enabled" value="true"><label for="enabledRadio" style="cursor: pointer;">Enabled</label></span>
						<span style="float: right; margin-right: 80px;"><input type="radio" id="disabledRadio" name="enabled" value="false" style="margin-left: 10px;"><label for="disabledRadio" style="cursor: pointer; margin-left: 10px;">Disabled</label></span>
					</span>
				</td>
			</tr>
			<tr>
				<td colspan="4">
					<button onclick="saveDevice();" style="float: right; margin-right: 110px; border-radius: 5px;">Save Device</button>
				</td>
			</tr>
		</tbody>
	</table>
</fieldset>
