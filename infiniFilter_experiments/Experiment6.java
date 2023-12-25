package infiniFilter_experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import filters.ChainedInfiniFilter;
import filters.DuplicatingChainedInfiniFilter;
import filters.Filter;
import filters.FingerprintGrowthStrategy.FalsePositiveRateExpansion;
import filters.QuotientFilter;

public class Experiment6 extends InfiniFilterExperiments {
	
	public static void main(String[] args) {
		parse_arguments(args);
		
		InfiniFilterExperiments.bits_per_entry = 8;
		num_entries_power = 10;	
		num_cycles = 27;
		FalsePositiveRateExpansion fpr_style = FalsePositiveRateExpansion.UNIFORM;
		
		
		baseline aleph_wide_res = new baseline();
		{
			//BasicInfiniFilter qf3 = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, true);
			//int fp_size = DuplicatingChainedInfiniFilter.derive_init_fingerprint_size(bits_per_entry - 3, 10);

			DuplicatingChainedInfiniFilter qf = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, true, -1);
			qf.set_expand_autonomously(true); 
			qf.set_fpr_style(fpr_style);
			long starting_index = 0;
			long end_key = qf.get_max_entries_before_expansion() - 1;
			Queue<Long> end_keys = new LinkedList<Long>();
			Queue<Long> start_keys = new LinkedList<Long>();
			for (int i = num_entries_power; i <= num_cycles - 4; i++ ) {
				end_keys.add(end_key);
				start_keys.add(starting_index);
				scalability_experiment(qf, starting_index, end_key,  aleph_wide_res);
				//if (qf.get_secondary() != null ) {
				if ( ! qf.is_chain_empty() ) {
					Long del_start_key = start_keys.remove();
					Long del_end_key = end_keys.remove();
					//qf.print_filter_summary();
					//qf.print_age_histogram();
					delete_oldest(qf, del_start_key, del_end_key, aleph_wide_res);
					//qf.print_filter_summary();
					//qf.print_age_histogram();
					//System.out.println();
				}
				//System.out.println(end_key);
				starting_index = end_key;
				end_key = qf.get_max_entries_before_expansion() * 2 - qf.get_num_void_entries() * 2 - 1;
				double percentage_full = qf.get_utilization();
				System.out.println("aleph fixed-width warmup " + i + "  " + qf.get_num_logical_entries() + "  " + percentage_full);
				long phs_slots = qf.get_physcial_num_slots();
				long bits_per_entry = qf.get_fingerprint_length() + 3;
				//qf.print_filter_summary();
				//System.out.println();
				//System.out.println("slots: " + phs_slots + "    bits  " + bits_per_entry);
				//qf.print_filter_summary();
				//qf.print_age_histogram();
				//System.out.println();
			}


		}
		aleph_wide_res.init();
		System.out.println("finished aleph fixed-width warmup");
		
		System.gc();
		
		baseline aleph_pred_res = new baseline();
		{
			//int expansions_est = (num_cycles - num_entries_power) / 2;
			DuplicatingChainedInfiniFilter qf = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, true, -1);
			qf.set_expand_autonomously(true); 
			qf.set_fpr_style(fpr_style);
			long starting_index = 0;
			long end_key = qf.get_max_entries_before_expansion() - 1;
			Queue<Long> end_keys = new LinkedList<Long>();
			Queue<Long> start_keys = new LinkedList<Long>();
			for (int i = num_entries_power; i <= num_cycles; i++ ) {
				end_keys.add(end_key);
				start_keys.add(starting_index);
				scalability_experiment(qf, starting_index, end_key,  aleph_pred_res);
				//if (qf.get_secondary() != null ) {
				if ( ! qf.is_chain_empty() ) {
					Long del_start_key = start_keys.remove();
					Long del_end_key = end_keys.remove();
					delete_oldest(qf, del_start_key, del_end_key, aleph_pred_res);
				}
				starting_index = end_key;
				long max_before_exp = qf.get_max_entries_before_expansion();
				long num_void = qf.get_num_void_entries();
				end_key = max_before_exp * 2 - num_void * 2 - 1;
				double percentage_full = qf.get_utilization();
				System.out.println("aleph fixed-width2 " + i + "  " + qf.get_num_logical_entries() + "  " + percentage_full);
				
				//System.out.println(starting_index + "  " + end_key + "  " + max_before_exp);
				///qf.print_filter_summary();
				//System.out.println();
				//System.out.println("slots: " + phs_slots + "    bits  " + bits_per_entry);

				//qf.print_filter_summary();
				//qf.print_age_histogram();
			}
			//qf.print_filter_summary();
			//qf.print_filter_summary();
			//qf.print_age_histogram();
		}
		//System.out.println("finished aleph predictive");
		
		System.gc();
		


		System.gc();
		System.out.println("finished aleph fixed-width");

		baseline chained_IF_res = new baseline();
		{
			ChainedInfiniFilter qf = new ChainedInfiniFilter(num_entries_power, bits_per_entry);
			qf.set_expand_autonomously(true); 
			qf.set_fpr_style(fpr_style);
			long starting_index = 0;
			long end_key = qf.get_max_entries_before_expansion() - 1;
			Queue<Long> end_keys = new LinkedList<Long>();
			Queue<Long> start_keys = new LinkedList<Long>();
			for (int i = num_entries_power; i <= num_cycles - 4; i++ ) {
				end_keys.add(end_key);
				start_keys.add(starting_index);
				scalability_experiment(qf, starting_index, end_key,  chained_IF_res);
				//if (qf.get_secondary() != null ) {
				if ( ! qf.is_chain_empty() ) {
					Long del_start_key = start_keys.remove();
					Long del_end_key = end_keys.remove();
					//qf.print_filter_summary();
					//qf.print_age_histogram();
					delete_oldest(qf, del_start_key, del_end_key, chained_IF_res);
					//qf.print_filter_summary();
					//qf.print_age_histogram();
					//System.out.println();
				}
				starting_index = end_key;
				end_key = qf.get_max_entries_before_expansion() * 2 - 1;
				double percentage_full = qf.get_utilization();
				System.out.println("chained IF " + i + "  " + qf.get_num_logical_entries() + "  " + percentage_full);
				//long phs_slots = qf.get_physcial_num_slots();
				//long bits_per_entry = qf.get_fingerprint_length() + 3;
				//System.out.println("slots: " + phs_slots + "    bits  " + bits_per_entry);
			}
			//qf.print_filter_summary();
			//qf.print_filter_summary();
			//qf.print_age_histogram();
		}	
		chained_IF_res.init();
		System.out.println("finished infinifilter fixed-width");
		System.gc();
		

		System.gc();

		baseline chained_IF_res2 = new baseline();
		{
			ChainedInfiniFilter qf = new ChainedInfiniFilter(num_entries_power, bits_per_entry);
			qf.set_expand_autonomously(true); 
			qf.set_fpr_style(fpr_style);
			long starting_index = 0;
			long end_key = qf.get_max_entries_before_expansion() - 1;
			Queue<Long> end_keys = new LinkedList<Long>();
			Queue<Long> start_keys = new LinkedList<Long>();
			for (int i = num_entries_power; i <= num_cycles; i++ ) {
				end_keys.add(end_key);
				start_keys.add(starting_index);
				scalability_experiment(qf, starting_index, end_key,  chained_IF_res2);
				//if (qf.get_secondary() != null ) {
				if ( ! qf.is_chain_empty() ) {
					Long del_start_key = start_keys.remove();
					Long del_end_key = end_keys.remove();
					//qf.print_filter_summary();
					//qf.print_age_histogram();
					delete_oldest(qf, del_start_key, del_end_key, chained_IF_res2);
					//qf.print_filter_summary();
					//qf.print_age_histogram();
					//System.out.println();
				}
				starting_index = end_key;
				end_key = qf.get_max_entries_before_expansion() * 2 - 1;
				double percentage_full = qf.get_utilization();
				System.out.println("chained IF " + i + "  " + qf.get_num_logical_entries() + "  " + percentage_full);
				//long phs_slots = qf.get_physcial_num_slots();
				//long bits_per_entry = qf.get_fingerprint_length() + 3;
				//System.out.println("slots: " + phs_slots + "    bits  " + bits_per_entry);
			}
			//qf.print_filter_summary();
			qf.print_filter_summary();
			qf.print_age_histogram();
		}	
		System.out.println("finished infinifilter fixed-width");
		System.gc();
		
		System.gc();



		int commas_before = 1;
		int commas_after = 5;
		chained_IF_res.print("num_entries", "insertion_time", commas_before++, commas_after--);
		chained_IF_res2.print("num_entries", "insertion_time", commas_before++, commas_after--);
		aleph_wide_res.print("num_entries", "insertion_time", commas_before++, commas_after--);
		aleph_pred_res.print("num_entries", "insertion_time", commas_before++, commas_after--);

		
		System.out.println();

		commas_before = 1;
		commas_after = 5;
		chained_IF_res.print("num_entries", "query_time", commas_before++, commas_after--);
		chained_IF_res2.print("num_entries", "query_time", commas_before++, commas_after--);
		aleph_wide_res.print("num_entries", "query_time", commas_before++, commas_after--);
		aleph_pred_res.print("num_entries", "query_time", commas_before++, commas_after--);

		
		System.out.println();

		commas_before = 1;
		commas_after = 5;
		chained_IF_res.print("num_entries", "FPR", commas_before++, commas_after--);
		chained_IF_res2.print("num_entries", "FPR", commas_before++, commas_after--);
		aleph_wide_res.print("num_entries", "FPR", commas_before++, commas_after--);
		aleph_pred_res.print("num_entries", "FPR", commas_before++, commas_after--);

		System.out.println();

		commas_before = 1;
		commas_after = 5;
		chained_IF_res.print("num_entries", "memory", commas_before++, commas_after--);
		chained_IF_res2.print("num_entries", "memory", commas_before++, commas_after--);
		aleph_wide_res.print("num_entries", "memory", commas_before++, commas_after--);
		aleph_pred_res.print("num_entries", "memory", commas_before++, commas_after--);
		
		System.out.println();
		
		commas_before = 1;
		commas_after = 5;
		chained_IF_res.print("num_entries", "delete_time", commas_before++, commas_after--);
		chained_IF_res2.print("num_entries", "delete_time", commas_before++, commas_after--);
		aleph_wide_res.print("num_entries", "delete_time", commas_before++, commas_after--);
		aleph_pred_res.print("num_entries", "delete_time", commas_before++, commas_after--);

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());

		LocalDate ld = java.time.LocalDate.now();
		String dir_name = "Exp6_" + bits_per_entry + "_bytes_" +  timeStamp.toString();
	    Path path = Paths.get(dir_name);

		try {
			Files.createDirectories(path);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String write_latency_file_name = dir_name + "/writes_speed.txt";
		String read_latency_file_name  = dir_name + "/read_speed.txt";
		String FPR_file_name  = dir_name + "/false_positive_rate.txt";
		String memory_file_name  = dir_name + "/memory.txt";
		String delete_latency_file_name = dir_name + "/deletes_speed.txt";
		String all_file_name  = dir_name + "/all.txt";
		
		create_file(write_latency_file_name);
		create_file(read_latency_file_name);
		create_file(FPR_file_name);
		create_file(memory_file_name);
		create_file(all_file_name);
		
	    try {
	        FileWriter insertion_writer = new FileWriter(write_latency_file_name);
	        
			commas_before = 1;
			commas_after = 5;
			chained_IF_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);
			chained_IF_res2.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);
			aleph_wide_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);
			aleph_pred_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);

			//System.out.println();
			insertion_writer.close();
	        FileWriter reads_writer = new FileWriter(read_latency_file_name);

			commas_before = 1;
			commas_after = 5;
			chained_IF_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);
			chained_IF_res2.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);
			aleph_wide_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);
			aleph_pred_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);

			//System.out.println();
			reads_writer.close();
	        FileWriter FPR_writer = new FileWriter(FPR_file_name);

			commas_before = 1;
			commas_after = 5;
			chained_IF_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, FPR_writer);
			chained_IF_res2.print_to_file("num_entries", "FPR", commas_before++, commas_after--, FPR_writer);
			aleph_wide_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, FPR_writer);
			aleph_pred_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, FPR_writer);

			FPR_writer.close();
			FileWriter mem_writer = new FileWriter(memory_file_name);
			
			//System.out.println();

			commas_before = 1;
			commas_after = 5;
			chained_IF_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, mem_writer);
			chained_IF_res2.print_to_file("num_entries", "memory", commas_before++, commas_after--, mem_writer);
			aleph_wide_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, mem_writer);
			aleph_pred_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, mem_writer);

			mem_writer.close();
			FileWriter del_writer = new FileWriter(delete_latency_file_name);
						
			commas_before = 1;
			commas_after = 5;
			chained_IF_res.print_to_file("num_entries", "delete_time", commas_before++, commas_after--, del_writer);
			chained_IF_res2.print_to_file("num_entries", "delete_time", commas_before++, commas_after--, del_writer);
			aleph_wide_res.print_to_file("num_entries", "delete_time", commas_before++, commas_after--, del_writer);
			aleph_pred_res.print_to_file("num_entries", "delete_time", commas_before++, commas_after--, del_writer);
			
			del_writer.close();
	    	FileWriter all_writer = new FileWriter(all_file_name);

			commas_before = 1;
			commas_after = 5;
			chained_IF_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, all_writer);
			chained_IF_res2.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, all_writer);
			aleph_wide_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, all_writer);
			aleph_pred_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, all_writer);

			all_writer.write("\n");
			
			commas_before = 1;
			commas_after = 5;
			chained_IF_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, all_writer);
			chained_IF_res2.print_to_file("num_entries", "query_time", commas_before++, commas_after--, all_writer);
			aleph_wide_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, all_writer);
			aleph_pred_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, all_writer);

			all_writer.write("\n");
			
			commas_before = 1;
			commas_after = 5;
			chained_IF_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, all_writer);
			chained_IF_res2.print_to_file("num_entries", "FPR", commas_before++, commas_after--, all_writer);
			aleph_wide_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, all_writer);
			aleph_pred_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, all_writer);

			all_writer.write("\n");
			
			commas_before = 1;
			commas_after = 5;
			chained_IF_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, all_writer);
			chained_IF_res2.print_to_file("num_entries", "memory", commas_before++, commas_after--, all_writer);
			aleph_wide_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, all_writer);
			aleph_pred_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, all_writer);

			all_writer.write("\n");

			commas_before = 1;
			commas_after = 5;
			chained_IF_res.print_to_file("num_entries", "delete_time", commas_before++, commas_after--, all_writer);
			chained_IF_res2.print_to_file("num_entries", "delete_time", commas_before++, commas_after--, all_writer);
			aleph_wide_res.print_to_file("num_entries", "delete_time", commas_before++, commas_after--, all_writer);
			aleph_pred_res.print_to_file("num_entries", "delete_time", commas_before++, commas_after--, all_writer);
			
			all_writer.close();
	    	
	        System.out.println("Successfully wrote to the files.");
	      } catch (IOException e) {
	        System.out.println("An error occurred.");
	        e.printStackTrace();
	      }
	}

	
	
	static public void scalability_experiment(Filter qf, long initial_key, long end_key, baseline results) {

		int num_qeuries = 1000000;
		int query_index = Integer.MAX_VALUE;
		int num_false_positives = 0;

		//int num_entries_to_insert = (int) (Math.pow(2, power) * (qf.expansion_threshold )) - qf.num_existing_entries;
		//final int initial_num_entries = qf.get_num_entries(true);
		
		long initial_num_entries = initial_key;
		long insertion_index = initial_key;
		long start_insertions = System.nanoTime();

		
		//System.out.println("inserting: " + num_entries_to_insert + " to capacity " + Math.pow(2, qf.power_of_two_size));

		boolean successful_insert = false;
		do {
			successful_insert = qf.insert(insertion_index, false);
			insertion_index++;
		} while (insertion_index < end_key && successful_insert);
		
		
		if (!successful_insert) {
			System.out.println("an insertion failed");
			System.exit(1);
		}
		
		//qf.pretty_print();

		long end_insertions = System.nanoTime();
		long start_queries = System.nanoTime();

		for (int i = 0; i < num_qeuries || num_false_positives < 10; i++) {
			boolean found = true;
			found = qf.search(query_index--);
			if (found) {
				num_false_positives++;
			}
			if (i > num_qeuries * 10) {
				break;
			}
		}
		num_qeuries = Integer.MAX_VALUE - query_index;

		long end_queries = System.nanoTime();
		double avg_insertions = (end_insertions - start_insertions) / (double)(insertion_index - initial_num_entries);
		double avg_queries = (end_queries - start_queries) / (double)num_qeuries;
		double FPR = num_false_positives / (double)num_qeuries;
		//int num_slots = (1 << qf.power_of_two_size) - 1;
		//double utilization = qf.get_utilization();
		//System.out.println("utilization  " + utilization);
		//double num_entries = qf.get_num_entries(true);
		double num_entries = end_key; // qf.get_num_occupied_slots(true);

		results.metrics.get("num_entries").add(num_entries);
		results.metrics.get("insertion_time").add(avg_insertions);
		results.metrics.get("query_time").add(avg_queries);
		results.metrics.get("FPR").add(FPR);
		double bits_per_entry = qf.measure_num_bits_per_entry();
		//System.out.println(bits_per_entry);
		results.metrics.get("memory").add(bits_per_entry);
		results.metrics.get("delete_time").add(0.0);
	}
	
	
	static public void delete_oldest(Filter qf, long initial_key, long end_key, baseline results) {
		
		//long initial_num_entries = initial_key;
		long delete_index = initial_key;
		long start_deletes = System.nanoTime();
		
		//System.out.println("inserting: " + num_entries_to_insert + " to capacity " + Math.pow(2, qf.power_of_two_size));

		long slot_of_deleted_key = -1;
		int num_deletes = 0;
		do {
			slot_of_deleted_key = qf.delete(delete_index);
			//boolean found = qf.search(delete_index);
			delete_index++;
			num_deletes++;
		} while (delete_index < end_key && slot_of_deleted_key > -1);
		
		if (slot_of_deleted_key == -1) {
			System.out.println("an delete failed");
			System.exit(1);
		}
		
		long end_deletes = System.nanoTime();
		
		double avg_deletes = (end_deletes - start_deletes) / (double)(num_deletes);
		//System.out.println("avg_deletes  " + avg_deletes);
		int last_index = results.metrics.get("delete_time").size() - 1;
		results.metrics.get("delete_time").set(last_index, avg_deletes);
		
		
	}
	
}
