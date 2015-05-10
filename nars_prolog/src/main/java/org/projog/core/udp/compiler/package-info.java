/**
 * Provides functionality to convert user defined predicates (defined using Prolog syntax) into Java classes.
 * <p>
 * As an intermediary step, the Prolog syntax is first translated into Java source code which is then compiled to bytecode.
 * The translation and compilation process happens dynamically at runtime in response to files containing Prolog syntax being consulted.
 */
package org.projog.core.udp.compiler;
