import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TesterClient {

	static public boolean check_equality(QuotientFilter qf, BitSet bs, boolean check_also_fingerprints) {
		for (int i = 0; i < bs.size(); i++) {
			if (check_also_fingerprints || (i % qf.bitPerEntry == 0 || i % qf.bitPerEntry == 1 || i % qf.bitPerEntry == 2)) {
				if (qf.get_bit_at_offset(i) != bs.get(i)) {
					System.out.println("failed test: bit " + i);
					System.exit(1);
				}
			}
		}
		return true;
	}
	
	// This test is based on the example from https://en.wikipedia.org/wiki/Quotient_filter
	// it performs the same insertions and query as the example and verifies that it gets the same results. 
	static public void test1() {
		int bits_per_entry = 8;
		int num_entries_power = 3;
		int num_entries = (int)Math.pow(2, num_entries_power);
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		long fingerprint0 = 0;
		long fingerprint1 = (1 << bits_per_entry) - 1;
		//System.out.println(fingerprint1);
	
		qf.insert(fingerprint0, 1, false);
		qf.insert(fingerprint1, 4, false);
		qf.insert(fingerprint0, 7, false);
		//qf.pretty_print();
		qf.insert(fingerprint0, 1, false);
		qf.insert(fingerprint0, 2, false);
		//qf.pretty_print();
		qf.insert(fingerprint0, 1, false);
		
		// these are the expecting resulting is_occupied, is_continuation, and is_shifted bits 
		// for all slots contigously. We do not store the fingerprints here
		BitSet result = new BitSet(num_entries * bits_per_entry);		
		result = set_slot_in_test(result, bits_per_entry, 0, false, false, false, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 1, true, false, false, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 2, true, true, true, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 3, false, true, true, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 4, true, false, true, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 5, false, false, true, fingerprint1);
		result = set_slot_in_test(result, bits_per_entry, 6, false, false, false, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 7, true, false, false, fingerprint0);
		//qf.pretty_print();
		
		check_equality(qf, result, true);
		
		if (qf.num_existing_entries != 6) {
			System.out.print("counter not working well");
			System.exit(1);
		}
	}
	
	
	// This test is based on the example from the quotient filter paper 
	// it performs the same insertions as in Figure 2 and checks for the same result
	static public void test2() {
		int bits_per_entry = 8;
		int num_entries_power = 4;
		int num_entries = (int)Math.pow(2, num_entries_power);
		QuotientFilter qf = new QuotientFilter(4, 8);
		
		qf.insert(0, 1, false);
		qf.insert(0, 1, false);
		qf.insert(0, 3, false);
		qf.insert(0, 3, false);
		qf.insert(0, 3, false);
		qf.insert(0, 4, false);
		qf.insert(0, 6, false);
		qf.insert(0, 6, false);
			
		BitSet result = new BitSet(num_entries * bits_per_entry);		
		result = set_slot_in_test(result, bits_per_entry, 0, false, false, false, 0);
		result = set_slot_in_test(result, bits_per_entry, 1, true, false, false, 0);
		result = set_slot_in_test(result, bits_per_entry, 2, false, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 3, true, false, false, 0);
		result = set_slot_in_test(result, bits_per_entry, 4, true, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 5, false, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 6, true, false, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 7, false, false, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 8, false, true, true, 0);
		check_equality(qf, result, false);
		
	}
	
	// Here we create a large(ish) filter, insert some random entries into it, and then make sure 
	// we get (true) positives for all entries we had inserted. 
	// This is to verify we do not get any false negatives. 
	// We then also check the false positive rate 
	static public void test3() {
		int bits_per_entry = 10;
		int num_entries_power = 5;
		int seed = 5; 
		//int num_entries = (int)Math.pow(2, num_entries_power);
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);
		HashSet<Integer> added = new HashSet<Integer>();
		Random rand = new Random(seed);
		double load_factor = 1.00;
		for (int i = 0; i < qf.get_physcial_num_slots() * load_factor; i++) {
			int rand_num = rand.nextInt();
			boolean success = qf.insert(rand_num, false);
			if (success) {
				added.add(rand_num);
			}
			else {
				System.out.println("insertion failed");
			}
			
		}
		//qf.print_important_bits();
		//qf.pretty_print();
		
		for (Integer i : added) {
			//System.out.println("searching  " + i );
			boolean found = qf.search(i);
			if (!found) {
				System.out.println("something went wrong!! seem to have false negative " + i);
				qf.search(i);
				System.exit(1);
			}
		}
		

	}
	
	static public void experiment_false_positives() {
		int bits_per_entry = 10;
		int num_entries_power = 5;
		int seed = 5; 
		//int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);
		HashSet<Integer> added = new HashSet<Integer>();
		Random rand = new Random(seed);
		double load_factor = 0.9;
		int num_queries = 20000;
		int num_false_positives = 0;
		
		for (int i = 0; i < qf.get_physcial_num_slots() * load_factor; i++) {
			int rand_num = rand.nextInt();
			boolean success = qf.insert(rand_num, false);
			if (success) {
				added.add(rand_num);
			}
			else {
				System.out.println("insertion failed");
			}
			
		}
		
		for (int i = 0; i < num_queries; i++) {
			int rand_num = rand.nextInt();
			if (!added.contains(rand_num)) {
				boolean found = qf.search(i);
				if (found) {
					//System.out.println("we seem to have a false positive");
					num_false_positives++;
				}
			}
		}
		double FPR = num_false_positives / (double)num_queries;
		System.out.println("measured FPR:\t" + FPR);
		double expected_FPR = Math.pow(2, - fingerprint_size);
		System.out.println("single fingerprint model:\t" + expected_FPR);
		double expected_FPR_bender = 1 - Math.exp(- load_factor / Math.pow(2, fingerprint_size));
		System.out.println("bender model:\t" + expected_FPR_bender);
	}
	
	static public void experiment_insertion_speed() {
		int bits_per_entry = 3;
		int num_entries_power = 12;
		int seed = 5; 
		//int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);
		Random rand = new Random(seed);
		double load_factor = 0.1;
		int num_queries = 20000;
		int num_false_positives = 0;
		double num_insertions = qf.get_physcial_num_slots() * load_factor; 
		long start = System.nanoTime();
		long time_sum = 0;
		long time_sum_square = 0;
		for (int i = 0; i < num_insertions; i++) {
			long start1 = System.nanoTime();
			int rand_num = rand.nextInt();
			boolean success = qf.insert(rand_num, false);

			long end1 = System.nanoTime(); 
			//if (i > 5) {
			long time_diff = (end1 - start1);
			time_sum += time_diff;
			time_sum_square += time_diff * time_diff; 
			//}
			//System.out.println("execution time :\t" + ( end1 - start1) / (1000.0) + " mic s");	
		}
		long end = System.nanoTime(); 
		System.out.println("execution time :\t" + ( end - start) / (1000.0 * 1000.0) + " ms");
		System.out.println("execution time per entry :\t" + ( end - start) / (num_insertions * 1000.0) + " mic sec");
		
		double avg_nano = time_sum / num_insertions;
		System.out.println("avg :\t" + (avg_nano / 1000.0));

		double avg_normalized = avg_nano / 1000.0;
		double time_sum_square_normalized = time_sum_square / 1000000.0 ;
		double variance = (time_sum_square_normalized - avg_normalized * avg_normalized * num_insertions) / num_insertions;
		double std = Math.sqrt(variance);
		System.out.println("std :\t" + std);
	}
	
	// adds two entries to the end of the filter, causing an overflow
	// checks this can be handled
	static public void test4() {
		int bits_per_entry = 8;
		int num_entries_power = 3;
		int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		long fp2 = 1 << fingerprint_size - 1;
	
		qf.insert(fp2, num_entries - 1, false);
		qf.insert(fp2, num_entries - 1, false);
		
		//qf.pretty_print();
		
		qf.delete(fp2, num_entries - 1);
		boolean found = qf.search(fp2, num_entries - 1);
		if (!found) {
			System.out.println("Should have found the entry");
			System.exit(1);
		}
	}
	
	static public BitSet set_slot_in_test(BitSet result, int bits_per_entry, int slot, boolean is_occupied, boolean is_continuation, boolean is_shifted, long fingerprint) {
		int index = bits_per_entry * slot;
		result.set(index++, is_occupied); 
		result.set(index++, is_continuation); 
		result.set(index++, is_shifted); 
		for (int i = 0; i < bits_per_entry - 3; i++) {
			result.set(index++, QuotientFilter.get_fingerprint_bit(i, fingerprint) );
		}
		return result;
	}
	
	static public BitSet set_slot_in_test(BitSet result, int bits_per_entry, int slot, boolean is_occupied, boolean is_continuation, boolean is_shifted, String fingerprint) {
		long l_fingerprint = 0;
		for (int i = 0; i < fingerprint.length(); i++) {
			char c = fingerprint.charAt(i);
			if (c == '1') {
				l_fingerprint |= (1 << i);
			}
		}
		
		return set_slot_in_test(result, bits_per_entry, slot, is_occupied, is_continuation, is_shifted, l_fingerprint);
	}

	// This is a test for deleting items. We insert many keys into one slot to create an overflow. 
	// we then remove them and check that the other keys are back to their canonical slots. 
	static public void test5() {
		int bits_per_entry = 8;
		int num_entries_power = 3;
		int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		long fp1 = 1 << 4;
		long fp2 = 1 << 3;
		long fp3 = 1 << 2;
	
		qf.insert(fp1, 1, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp2, 2, false);
		qf.insert(fp3, 4, false);
		
		//qf.pretty_print();
		qf.delete(fp1, 1);
		qf.delete(fp1, 1);
		qf.delete(fp1, 1);
		qf.delete(fp1, 1);
		qf.delete(fp1, 1);

		BitSet result = new BitSet(num_entries * bits_per_entry);	
		result = set_slot_in_test(result, bits_per_entry, 2, true, false, false, fp2);
		result = set_slot_in_test(result, bits_per_entry, 4, true, false, false, fp3);
		check_equality(qf, result, true);
		//qf.pretty_print();
	}
	
	static public void test6() {
		
		int bits_per_entry = 8;
		int num_entries_power = 4;
		int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);
		
		qf.insert(0, 2, false);
		qf.insert(0, 3, false);
		qf.insert(0, 3, false);
		//qf.pretty_print();
		qf.insert(0, 4, false);
		//qf.insert(0, 2, false);
		//qf.pretty_print();
		//qf.insert(0, 1, false);
		
		Iterator it = new Iterator(qf);
		int[] arr = new int[] {2, 3, 3, 4};
        int arr_index = 0;
		
		while (it.next()) {
			//System.out.println(it.bucket_index);
			if (arr[arr_index++] != it.bucket_index) {
				System.out.print("error in iteration");
				System.exit(1);
			}
		}
		
	}
	
	static public void test7() {
		
		int bits_per_entry = 8;
		int num_entries_power = 4;
		int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);
		
		qf.insert(0, 1, false);
		qf.insert(0, 4, false);
		qf.insert(0, 7, false);
		//qf.pretty_print();
		qf.insert(0, 1, false);
		qf.insert(0, 2, false);
		//qf.pretty_print();
		qf.insert(0, 1, false);
		
		Iterator it = new Iterator(qf);
		int[] arr = new int[] {1, 1, 1, 2, 4, 7};
        int arr_index = 0;
        //qf.pretty_print();
		
		while (it.next()) {
			//System.out.println(it.bucket_index);
			if (arr[arr_index++] != it.bucket_index) {
				System.out.print("error in iteration");
				System.exit(1);
			}
		}
	}
	
	// In this test, we create one FingerprintShrinkingQF and expand it once.
	// We also create an expanded Quotient Filter with the same data from the onset and make sure they are logically equivalent. 
	static public void test8() {
		
		int bits_per_entry = 10;
		int num_entries_power = 4;
		FingerprintShrinkingQF qf = new FingerprintShrinkingQF(num_entries_power, bits_per_entry);
		qf.max_entries_before_expansion = Integer.MAX_VALUE; // disable automatic expansion
		//qf.print_key(1);
		
		for (int i = 0; i < 12; i++) {
			qf.insert(i, false);
		}
		
		//qf.pretty_print();
		qf.expand();
		//qf.pretty_print();
		
		QuotientFilter qf2 = new QuotientFilter(num_entries_power + 1, bits_per_entry - 1);
		
		for (int i = 0; i < 12; i++) {
			qf2.insert(i, false);
		}
		
		//qf2.pretty_print();
		
		if (qf.filter.size() != qf2.filter.size()) {
			System.out.print("filters have different sizes");
			System.exit(1);
		}
		
		for (int i = 0; i < qf.get_logical_num_slots(); i++) {
			Set<Long> set1 = qf.get_all_fingerprints(i);
			Set<Long> set2 = qf2.get_all_fingerprints(i);
			
			if (!set1.equals(set2)) {
				System.out.print("fingerprints for bucket " + i + " not identical");
				System.exit(1);
			}
		}
	}
	
	// insert entries across two phases of expansion, and then check we can still find all of them
	static public void test9() {
		
		int bits_per_entry = 10;
		int num_entries_power = 3;
		MultiplyingQF qf = new MultiplyingQF(num_entries_power, bits_per_entry);
		qf.max_entries_before_expansion = Integer.MAX_VALUE; // disable automatic expansion

		int i = 0;
		while (i < Math.pow(2, num_entries_power) - 2) {
			qf.insert(i, false);
			i++;
		}
		qf.expand();
		while (i < Math.pow(2, num_entries_power + 1) - 2) {
			qf.insert(i, false);
			i++;
		}
		
		for (int j = 0; j < i; j++) {
			if ( !qf.search(j) ) {
				System.out.println("false negative  " + j);
				System.exit(1);
			}
		}
		
	}
	
	static public void test10() {
		int bits_per_entry = 10;
		int num_entries_power = 3;
		
		int fingerprint_size = bits_per_entry - 3;  
		ExpandableQF qf = new ExpandableQF(num_entries_power, bits_per_entry);
		
		// test we can parse all the different unary codes up to 
		long fingerprint_i = fingerprint_size;
		long reversed_unary = 0;
		for (int i = 0; i < fingerprint_size; i++) {
			long num = i;
			reversed_unary |= (long) Math.pow(2, fingerprint_i--);
			long num_verified = qf.parse_unary(reversed_unary);
			//System.out.println(num + " " + reversed_unary +  "  " + num_verified);
			if (num != num_verified) {
				System.out.println("unary matching not working");
				System.exit(1);
			}
		}	

		int i = 1;
		while (i < Math.pow(2, num_entries_power) - 1) {
			qf.insert(i, false);
			i++;
		}
		
		qf.pretty_print();
		qf.expand();
		qf.pretty_print();

		
		int num_entries = 1 << ++num_entries_power;
		BitSet result = new BitSet(num_entries * bits_per_entry);		
		result = set_slot_in_test(result, bits_per_entry, 0, false, false, false, "0000000");
		result = set_slot_in_test(result, bits_per_entry, 1, true, false, false, "1100101");
		result = set_slot_in_test(result, bits_per_entry, 2, true, false, false, "1010101");
		result = set_slot_in_test(result, bits_per_entry, 3, false, false, false, "0000000");
		result = set_slot_in_test(result, bits_per_entry, 4, false, false, false, "0000000");
		result = set_slot_in_test(result, bits_per_entry, 5, true, false, false, "0010001");
		result = set_slot_in_test(result, bits_per_entry, 6, false, false, false, "0000000");
		result = set_slot_in_test(result, bits_per_entry, 7, true, false, false, "0101101");
		result = set_slot_in_test(result, bits_per_entry, 8, true, false, false, "1001001");
		result = set_slot_in_test(result, bits_per_entry, 9, false, true, true, "0111001");
		check_equality(qf, result, true);
	}
	
	static public  void main(String[] args) {
		test1(); // example from wikipedia
		test2(); // example from quotient filter paper
		test3(); // ensuring no false negatives
		test4(); // overflow test
		test5(); // deletion test 
		test6(); // iteration test 1
		test7(); // iteration test 2
		test8(); // expansion test for FingerprintShrinkingQF
		test9(); // expansion test for MultiplyingQF
		test10(); // expansion test for MultiplyingQF
		
		//experiment_false_positives();
		//experiment_insertion_speed();
	}
	
	
}
