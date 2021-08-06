# LittleCompiler
A compiler for the little language, targeted for the tiny architecture.

There are three files in the LanguageDocs folder that describe the language and architecture.
-- tokens.txt lists the tokens for the language. <br />
-- grammar.txt is the CFG for the language. <br />
-- tinyDoc.txt details the architecture for tiny, including is registers and instructions.

This project first starts with the little.g4 file which contains the lexer rules as regular expressions and the parser rules as a context free grammar. This file with ANTLR generates the needed lexer and parser for the compiler. Using the ANTLR generated walker/listener methods a traversal of the abstract syntax tree is preformed to generated the IR code. From here the machine specific assembly code is generated in this case for the tiny architecture.

One item to note is that this is not a complete compiler for the little language. Many of its constructs are missing from final code generation. The lexer and parser are complete, but the final code generation only handles variable assignment, input, output, and simple expression calculations. Functions, control structures and complex expressions are not yet complete.

# Compilation

Compiler Component (Java)

This project needs ANTLR 4 for compilation of the .g4 file and for later use in the compiler source code. Full detail can be found here: https://www.antlr.org/

The project is compiled in java14.

Simulator (C++)

The simulator code should be compiled with g++-10.

# Usage

For the compiler little source code should be piped to stdin. This will later be changed to a file input.

For the simulator the generated tiny assembly code should be passed as an argument.

# Future 

I plan to add the full language specifications to the project in the future.

