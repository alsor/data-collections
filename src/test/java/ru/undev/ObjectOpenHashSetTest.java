package ru.undev;

import static org.junit.Assert.assertFalse;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

public class ObjectOpenHashSetTest {

	@Test
	@Ignore
	public void jdk_hash_set() throws Exception {
		byte[] b1 = new byte[] { 1, 2, 3 };
		byte[] b2 = new byte[] { 1, 2, 3 };

		Set<byte[]> set = new HashSet<byte[]>();

		set.add(b1);
		assertFalse(set.add(b2));
	}

	@Test
	@Ignore
	public void not_contain_duplicates_when_used_for_byte_arrays() throws Exception {
		byte[] b1 = new byte[] { 1, 2, 3 };
		byte[] b2 = new byte[] { 1, 2, 3 };

		ObjectSet<byte[]> set = new ObjectOpenHashSet<byte[]>();

		set.add(b1);
		assertFalse(set.add(b2));
	}

	@Test
	public void fix_previous_with_custom_hash_set() throws Exception {
		byte[] b1 = new byte[] { 1, 2, 3 };
		byte[] b2 = new byte[] { 1, 2, 3 };

		@SuppressWarnings("unchecked")
		ObjectSet<byte[]> set = new ObjectOpenCustomHashSet<byte[]>(ByteArrays.HASH_STRATEGY);

		set.add(b1);
		assertFalse(set.add(b2));
	}

	@Test
	public void not_contain_duplicates_when_used_for_strings() throws Exception {
		String s1 = "abc";
		String s2 = "abc";

		ObjectSet<String> set = new ObjectOpenHashSet<String>();

		set.add(s1);
		assertFalse(set.add(s2));
	}

}
