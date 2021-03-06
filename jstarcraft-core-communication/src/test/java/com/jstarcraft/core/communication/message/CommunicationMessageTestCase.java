package com.jstarcraft.core.communication.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.jstarcraft.core.codec.ContentCodec;
import com.jstarcraft.core.codec.JsonContentCodec;
import com.jstarcraft.core.codec.KryoContentCodec;
import com.jstarcraft.core.codec.ProtocolContentCodec;
import com.jstarcraft.core.codec.specification.CodecDefinition;

public class CommunicationMessageTestCase {

	@Test
	public void testMessageCodec() throws IOException {
		Collection<Type> protocolClasses = new LinkedList<>();
		protocolClasses.add(MockComplexObject.class);
		protocolClasses.add(MockEnumeration.class);
		protocolClasses.add(MockSimpleObject.class);
		CodecDefinition protocolDefinition = CodecDefinition.instanceOf(protocolClasses);

		Map<Byte, ContentCodec> codecs = new HashMap<>();
		JsonContentCodec jsonContentCodec = new JsonContentCodec(protocolDefinition);
		codecs.put(MessageFormat.JSON.getMark(), jsonContentCodec);

		ProtocolContentCodec protocolContentCodec = new ProtocolContentCodec(protocolDefinition);
		codecs.put(MessageFormat.PROTOCOL_BUFFER_X.getMark(), protocolContentCodec);

		KryoContentCodec kyroContentCodec = new KryoContentCodec(protocolDefinition);
		codecs.put(MessageFormat.KRYO.getMark(), kyroContentCodec);

		MockComplexObject content = MockComplexObject.instanceOf(0, "birdy", "hong", 10, Instant.now(), MockEnumeration.TERRAN);
		testMessageCodec(MessageFormat.JSON, content, codecs);
		testMessageCodec(MessageFormat.PROTOCOL_BUFFER_X, content, codecs);
		testMessageCodec(MessageFormat.KRYO, content, codecs);

		Object[] contents = new MockComplexObject[] { content };
		testMessageCodec(MessageFormat.JSON, contents, codecs);
		testMessageCodec(MessageFormat.PROTOCOL_BUFFER_X, contents, codecs);
		testMessageCodec(MessageFormat.KRYO, content, codecs);

		testMessageCodec(MessageFormat.JSON, null, codecs);
		testMessageCodec(MessageFormat.PROTOCOL_BUFFER_X, null, codecs);
		testMessageCodec(MessageFormat.KRYO, null, codecs);

		String string = "eyJhbGciOiJIUzUxMiJ9.eyJ1aWQiOjEsImlzcyI6Imh0dHA6Ly9zdmxhZGEuY29tIiwidGltZSI6IjIwMTctMDYtMTQgMDk6MDE6MzQiLCJleHAiOjE0OTc0MDI5OTQsImlhdCI6MTQ5NzQwMjA5NH0.8fELQQ-iAwa6Ue_pGUi8qzTu7tHScAxPBzp92ZWJDII9I4eXftXAaHF2qb8UgGzrbTPa_baIa7MKFLwujfZ16Q,eyJhbGciOiJIUzUxMiJ9.eyJ1aWQiOjEsImlzcyI6Imh0dHA6Ly9zdmxhZGEuY29tIiwidGltZSI6IjIwMTctMDYtMTQgMDk6MDE6MzQiLCJzY29wZXMiOlsiUk9MRV9SRUZSRVNIX1RPS0VOIl0sImV4cCI6MTQ5NzQwNTY5NSwiaWF0IjoxNDk3NDAyMDk1LCJqdGkiOiIyNTFiYmViYy01NmNhLTQxOTUtOWY5OC0yMmU2ZDI3MWFkMmIifQ.2pu4DfpstAwRNVAH5mzzNg9fhNB-etuH9Zm6yc18C2LjDxzjCmUeZVVvntPvhcNAXNUma34L_t7CRtgSVCNc-Q";
		testMessageCodec(MessageFormat.JSON, string, codecs);
		testMessageCodec(MessageFormat.PROTOCOL_BUFFER_X, string, codecs);
		testMessageCodec(MessageFormat.KRYO, string, codecs);
	}

	private void testMessageCodec(MessageFormat format, Object content, Map<Byte, ContentCodec> codecs) throws IOException {
		ContentCodec codec = codecs.get(format.getMark());
		byte[] data = codec.encode(content == null ? void.class : content.getClass(), content);
		MessageHead head = MessageHead.instanceOf(1, (byte) 1, (byte) 1);
		MessageBody body = MessageBody.instanceOf(true, format, data);
		MessageTail tail = MessageTail.instanceOf(10);
		CommunicationMessage left = CommunicationMessage.instanceOf(head, body, tail);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
		CommunicationMessage.writeTo(dataOutputStream, left);
		data = byteArrayOutputStream.toByteArray();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
		CommunicationMessage right = CommunicationMessage.readFrom(dataInputStream);
		Assert.assertEquals(left, right);
	}

}
