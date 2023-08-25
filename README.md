# Search Engine Project

## Overview

This repository contains a search engine project that aims to provide efficient information retrieval and ranking capabilities. The project utilizes various features to enhance search functionality and deliver accurate results. It employs object-oriented programming principles, integrates with an SQL database, and includes unit testing with JUnit to ensure reliability and performance.

## Features Implemented

- **Boolean and Ranked Retrieval Modes**: The search engine operates in two modes: Build Index and Query Index. It supports both Boolean and Ranked querying styles, allowing users to retrieve relevant documents based on their preferences.

- **Positional Inverted Index**: The core indexing mechanism involves creating a positional inverted index, which efficiently maps terms to their occurrences within documents, enabling faster retrieval and ranking.

- **Text Preprocessing**: Text preprocessing techniques like stemming, stop words and case folding are employed to standardize terms, improving the accuracy of queries.

- **Spelling Correction**: The search engine includes a spelling correction module that suggests modified queries with corrected spellings for terms with low document frequency or those missing from the vocabulary.

- **Disk-Based Index**: The search engine uses the SPIMI algorithm to create an on-disk index, facilitating efficient storage and retrieval of large volumes of data.

- **Dynamic Indexing**: Dynamic indexing is implemented using logarithmic merging, allowing the index to handle changes to the corpus at runtime.

- **Variant TF-IDF Formulas**: Different weighting schemes for calculating term and document weights (wq,t, wd,t, Ld) are supported, offering flexibility in retrieval strategies.

- **Variable Byte Encoding**: Index files are encoded using variable byte encoding, optimizing storage efficiency.

- **DSP Index**: The Documents, Scores, Positions (DSP) index optimizes score computation by precomputing wd,t values and storing them alongside postings.

- **NOT Queries**: Boolean queries include support for NOT operations, enhancing query capabilities.

- **NEAR/K Operator**: The NEAR/K operator enhances query precision by allowing users to specify a proximity constraint between terms.

- **Precision-Recall Evaluation**: A performance-oriented evaluation assesses Mean Average Precision and other metrics to measure the search engine's effectiveness.

- **Impact Ordering**: An impact-ordered index optimizes ranking performance by sorting postings lists in decreasing tft,d order.

- **Object-Oriented Programming**: The project is developed using object-oriented programming principles, ensuring modularity, extensibility, and maintainability.

- **SQL Database Integration**: The search engine leverages an SQL database to manage and query index data efficiently.

- **JUnit Testing**: Comprehensive unit tests are implemented using JUnit, validating the correctness and reliability of different functionalities.

## Usage

To use the search engine, follow these steps:

1. Clone the repository to your local machine.
2. Install any required dependencies as specified in the project documentation.
3. Run the project and interact with the search engine through the provided user interface.

## License

This project is licensed under the MIT License. You can find more details in the LICENSE file.

