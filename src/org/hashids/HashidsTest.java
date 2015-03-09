package org.hashids;

import org.junit.Test;

public class HashidsTest {
	
	@Test
	public void test_new_salt() {
		long num_to_hash = 0;
		Hashids a = new Hashids("MoLg1gOeHl6Bl8HyDTtb8CVC5fFRqX9S",6);
		String resString = a.encode(num_to_hash);
		System.out.println(resString);
		long[] b = a.decode(resString);
		System.out.println(b[0]);
	}
}

