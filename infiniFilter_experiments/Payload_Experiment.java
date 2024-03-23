/*
 * Copyright 2024 Niv Dayan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package infiniFilter_experiments;

import filters.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
// add warm up
// check for bigger fingerprints
// test for both mirroring and beside fingerprint

public class Payload_Experiment extends ExperimentsBase {
    public static void main(String[] args) {
        parse_arguments(args);

        System.gc();
        {
            QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry, payload_size);
            scalability_experiment(qf, 0, qf.get_max_entries_before_expansion() - 1, new baseline());
        }
        {
            QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry, payload_size);
            scalability_experiment(qf, 0, qf.get_max_entries_before_expansion() - 1, new baseline());
        }
        {
            QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry, payload_size);
            scalability_experiment(qf, 0, qf.get_max_entries_before_expansion() - 1, new baseline());
        }


        int[] payload_sizes = {
                0,
                32,
                512,
        };
        for (int ps : payload_sizes) {
            System.gc();
            payload_size = ps;
            System.out.println(payload_size);
            baseline original_qf_res = new baseline();
            {
                int qf_size = (num_entries_power + num_cycles) / 2;
                QuotientFilter orig = new QuotientFilter(qf_size, bits_per_entry, payload_size);
                orig.set_expand_autonomously(false);
                long starting_index = 0;
                for (int i = 1; i < num_cycles; i++) {
                    long end_key = (int) (Math.pow(2, qf_size) * i / num_cycles); //
                    scalability_experiment(orig, starting_index, end_key, original_qf_res);
                    starting_index = end_key;
                    System.out.println("static quotient filter " + i);
                }
            }

            System.gc();
            System.out.println("finished quotient");

//            baseline chained_IF_res = new baseline();
//            {
//                BasicInfiniFilter qf = new ChainedInfiniFilter(num_entries_power, bits_per_entry, payload_size);
//                qf.set_expand_autonomously(true);
//                long starting_index = 0;
//                long end_key = qf.get_max_entries_before_expansion() - 1;
//                for (int i = num_entries_power; i <= num_cycles; i++) {
//                    scalability_experiment(qf, starting_index, end_key, chained_IF_res);
//                    starting_index = end_key;
//                    end_key = qf.get_max_entries_before_expansion() * 2 - 1;
//                    System.out.println("infinifilter " + i);
//                }
//            }
//            System.out.println("finished infinifilter");
//            System.gc();
//
//            baseline bit_sacrifice_res = new baseline();
//            {
//                FingerprintSacrifice qf2 = new FingerprintSacrifice(num_entries_power, bits_per_entry, payload_size);
//                qf2.set_expand_autonomously(true);
//                long starting_index = 0;
//                long end_key = qf2.get_max_entries_before_expansion() - 1;
//                for (int i = num_entries_power; i <= num_cycles && qf2.get_fingerprint_length() > 0; i++) {
//                    scalability_experiment(qf2, starting_index, end_key, bit_sacrifice_res);
//                    starting_index = end_key;
//                    end_key = qf2.get_max_entries_before_expansion() * 2 - 1;
//                    System.out.println("bit sacrifice " + i);
//                }
//            }
//            System.out.println("finished bit sacrifice");
//
//            System.gc();
//
//            baseline geometric_expansion_res = new baseline();
//            {
//                Chaining qf3 = new Chaining(num_entries_power, bits_per_entry, payload_size);
//                qf3.set_expand_autonomously(true);
//                long starting_index = 0;
//                long end_key = qf3.get_max_entries_before_expansion() - 1;
//                for (int i = num_entries_power; i <= num_cycles - 1; i++) {
//                    scalability_experiment(qf3, starting_index, end_key, geometric_expansion_res);
//                    starting_index = end_key + 1;
//                    end_key = (long) (qf3.get_max_entries_before_expansion() * 2 + starting_index - 1);
//                    //System.out.println("thresh  " + qf3.max_entries_before_expansion);
//
//                    //(long)(Math.pow(2, power_of_two_size) * expansion_threshold)
//                    System.out.println("geometric chaining " + i);
//                }
//            }
//            System.out.println("finished geometric chaining");
//
            int commas_before = 1;
            int commas_after = 5;
            System.out.println("Insertion Time");
            original_qf_res.print("num_entries", "insertion_time", commas_before++, commas_after--);
            // chained_IF_res.print("num_entries", "insertion_time", commas_before++, commas_after--);
            // bit_sacrifice_res.print("num_entries", "insertion_time", commas_before++, commas_after--);
            // geometric_expansion_res.print("num_entries", "insertion_time", commas_before++, commas_after--);

            System.out.println();

            commas_before = 1;
            commas_after = 5;
            System.out.println("Query Time");
            original_qf_res.print("num_entries", "query_time", commas_before++, commas_after--);
            // chained_IF_res.print("num_entries", "query_time", commas_before++, commas_after--);
            // bit_sacrifice_res.print("num_entries", "query_time", commas_before++, commas_after--);
            // geometric_expansion_res.print("num_entries", "query_time", commas_before++, commas_after--);

            System.out.println();

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());

            LocalDate ld = LocalDate.now();
            String dir_name = "Exp1_" + bits_per_entry + "_bytes_" + timeStamp.toString();
            Path path = Paths.get(dir_name);

            try {
                Files.createDirectories(path);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            String write_latency_file_name = dir_name + "/writes_speed.txt";
            String read_latency_file_name = dir_name + "/read_speed.txt";

            create_file(write_latency_file_name);
            create_file(read_latency_file_name);

            try {
                FileWriter insertion_writer = new FileWriter(write_latency_file_name);

                commas_before = 1;
                commas_after = 5;
                original_qf_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);
                // chained_IF_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);
                // bit_sacrifice_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);
                // geometric_expansion_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);

                //System.out.println();
                insertion_writer.close();
                FileWriter reads_writer = new FileWriter(read_latency_file_name);

                commas_before = 1;
                commas_after = 5;
                original_qf_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);
                // chained_IF_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);
                // bit_sacrifice_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);
                // geometric_expansion_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);

                System.out.println("Successfully wrote to the files.");
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }

        }
    }

    static public void scalability_experiment(Filter qf, long initial_key, long end_key, baseline results) {
        int num_false_positives = 0;

        // Generate random payloads before starting the time measurement
        long[][] payloads = new long[(int) (end_key - initial_key)][(payload_size + Long.SIZE - 1) / Long.SIZE]; // Assuming end_key > initial_key
        for (int i = 0; i < end_key - initial_key; i++) {
            payloads[i] = generateRandomPayload(payload_size); // Specify the payload size as per your requirement
            System.out.println(Arrays.toString(payloads[i]));
        }

        long initial_num_entries = initial_key;
        long insertion_index = initial_key;
        long start_insertions = System.nanoTime();

        boolean successful_insert = false;
        int payloadIndex = 0; // Index to keep track of which payload to insert
        do {
            // Use pre-generated payloads instead of generating them during insertion
            successful_insert = qf.insert(insertion_index, false, payloads[payloadIndex++]);
            insertion_index++;
        } while (insertion_index < end_key && successful_insert);

        if (!successful_insert) {
            System.out.println("an insertion failed");
            System.exit(1);
        }

        long end_insertions = System.nanoTime();
        long start_queries = System.nanoTime();
        long read_this = initial_num_entries;
        long start_key;
        for (start_key = read_this; start_key <= end_key; start_key++) {
            long[][] found_payloads = qf.search_for_payloads(start_key);
        }
        long num_queries = start_key - read_this;
        long end_queries = System.nanoTime();
        double avg_insertions = (end_insertions - start_insertions) / (double) (insertion_index - initial_num_entries);
        double avg_queries = (end_queries - start_queries) / (double) num_queries;
        double num_entries = qf.get_num_occupied_slots(true);

        results.metrics.get("num_entries").add(num_entries);
        results.metrics.get("insertion_time").add(avg_insertions);
        results.metrics.get("query_time").add(avg_queries);
    }
}
