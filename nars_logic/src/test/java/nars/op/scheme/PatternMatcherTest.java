package nars.op.scheme;

public class PatternMatcherTest {
	// @Test
	// public void wildcard() {
	// assertThat(matches(ImmutableList.of(), LinkedListMultimap.create(),
	// read("_"), read("foo")), is(true));
	// }
	//
	// @Test
	// public void pattern_variable() {
	// LinkedListMultimap<String, Expression> bindings =
	// LinkedListMultimap.create();
	// assertThat(matches(ImmutableList.of(), bindings, read("a"), read("foo")),
	// is(true));
	// assertThat(bindings.get("a").get(0), is(symbol("foo")));
	// }
	//
	// @Test
	// public void literal_identifier() {
	// assertThat(matches(ImmutableList.of("else"), LinkedListMultimap.create(),
	// read("else"), read("else")), is(true));
	// }
	//
	// @Test
	// public void empty_list() {
	// assertThat(matches(ImmutableList.of(), LinkedListMultimap.create(),
	// read("()"), read("()")), is(true));
	// }
	//
	// @Test
	// public void one_element_list_with_wildcard() {
	// assertThat(matches(ImmutableList.of(), LinkedListMultimap.create(),
	// read("(_)"), read("(a)")), is(true));
	// }
	//
	// @Test
	// public void two_element_list_with_wildcard() {
	// LinkedListMultimap<String, Expression> bindings =
	// LinkedListMultimap.create();
	// assertThat(matches(ImmutableList.of(), bindings, read("(_ a)"),
	// read("(foo 1")), is(true));
	// assertThat(bindings.get("a").get(0), is(number(1)));
	// }
	//
	// @Test
	// public void nested_list() {
	// LinkedListMultimap<String, Expression> bindings =
	// LinkedListMultimap.create();
	// assertThat(matches(ImmutableList.of(), bindings,
	// read("(_ ((a b) (c d))"), read("(foo ((1 2) (3 4))")), is(true));
	// assertThat(bindings.get("a").get(0), is(number(1)));
	// assertThat(bindings.get("b").get(0), is(number(2)));
	// assertThat(bindings.get("c").get(0), is(number(3)));
	// assertThat(bindings.get("d").get(0), is(number(4)));
	// }
	//
	// @Test
	// public void ellipsis() {
	// assertThat(matches(ImmutableList.of(), LinkedListMultimap.create(),
	// read("(_ ...)"), read("(foo 1 2 3 4)")), is(true));
	// }
	//
	// @Test
	// public void ellipsis_with_tail() {
	// LinkedListMultimap<String, Expression> bindings =
	// LinkedListMultimap.create();
	// assertThat(matches(ImmutableList.of(), bindings, read("(_ a b ... c d)"),
	// read("(foo 1 2 3 4)")), is(true));
	// assertThat(bindings.get("a").get(0), is(number(1)));
	// assertThat(bindings.get("b").get(0), is(number(2)));
	// assertThat(bindings.get("c").get(0), is(number(3)));
	// assertThat(bindings.get("d").get(0), is(number(4)));
	// }
	//
	// @Test
	// public void ellipsis_with_multiple_bindings() {
	// LinkedListMultimap<String, Expression> bindings =
	// LinkedListMultimap.create();
	// assertThat(matches(ImmutableList.of(), bindings, read("(_ a ...)"),
	// read("(foo 1 2)")), is(true));
	// assertThat(bindings.get("a").get(0), is(number(1)));
	// assertThat(bindings.get("a").get(1), is(number(2)));
	// }
	//
	// @Test
	// public void ellipsis_with_multiple_bindings_and_tail() {
	// LinkedListMultimap<String, Expression> bindings =
	// LinkedListMultimap.create();
	// assertThat(matches(ImmutableList.of(), bindings, read("(_ a ... b)"),
	// read("(foo 1 2 3)")), is(true));
	// assertThat(bindings.get("a").get(0), is(number(1)));
	// assertThat(bindings.get("a").get(1), is(number(2)));
	// assertThat(bindings.get("b").get(0), is(number(3)));
	// }
	//
	// @Test
	// public void list_ellipsis() {
	// LinkedListMultimap<String, Expression> bindings =
	// LinkedListMultimap.create();
	// assertThat(matches(ImmutableList.of(), bindings, read("((a b) ...)"),
	// read("((1 2) (3 4))")), is(true));
	// assertThat(bindings.get("a").get(0), is(number(1)));
	// assertThat(bindings.get("a").get(1), is(number(3)));
	// assertThat(bindings.get("b").get(0), is(number(2)));
	// assertThat(bindings.get("b").get(1), is(number(4)));
	// }
	//
	// @Test
	// public void simple_template() {
	// assertThat(expandTemplate(LinkedListMultimap.create(), read("(+ 1 2)")),
	// is(read("(+ 1 2)")));
	// }
	//
	// @Test
	// public void template_with_binding() {
	// assertThat(expandTemplate(LinkedListMultimap.create(ImmutableMultimap.of("a",
	// symbol("foo"))), read("(a)")), is(read("(foo)")));
	// }
	//
	// public static Expression expandTemplate(LinkedListMultimap<String,
	// Expression> bindings, Expression template) {
	// if (template.isSymbol()) {
	// if (bindings.containsKey(template.symbol().value)) {
	// return bindings.get(template.symbol().value).remove(0);
	// }
	// } else if (template.isList()) {
	// return ListExpression.list(template.list().value.stream().map(e ->
	// expandTemplate(bindings, e)).collect(Cons.collector()));
	// }
	// return template;
	// }
	//
	// public static boolean matches(List<String> literals, Multimap<String,
	// Expression> bindings, Expression pattern, Expression candidate) {
	// if (pattern.isSymbol()) {
	// String patternSymbol = pattern.symbol().value;
	// if (patternSymbol.equals("_")) {
	// return true;
	// }
	//
	// if (!literals.contains(patternSymbol)) {
	// bindings.put(patternSymbol, candidate);
	// return true;
	// }
	//
	// if (candidate.equals(pattern)) {
	// return true;
	// }
	//
	// } else if (pattern.isList()) {
	// if (candidate.isList()) {
	// return matchesSequence(literals, bindings, pattern.list().value,
	// candidate.list().value);
	// }
	// }
	// return false;
	// }
	//
	// private static boolean matchesSequence(List<String> literals,
	// Multimap<String, Expression> bindings, Cons<Expression> pattern,
	// Cons<Expression> candidate) {
	// if (pattern.stream().anyMatch(e -> e.equals(symbol("...")))) {
	// return matchSequenceWithEllipsis(literals, bindings, pattern, candidate);
	//
	// } else {
	// if (pattern.size() != candidate.size()) {
	// return false;
	// }
	//
	// Iterator<Expression> ce = candidate.iterator();
	// return pattern.stream().allMatch(p -> matches(literals, bindings, p,
	// ce.next()));
	// }
	// }
	//
	// private static boolean matchSequenceWithEllipsis(List<String> literals,
	// Multimap<String, Expression> bindings, Cons<Expression> pattern,
	// Cons<Expression> candidate) {
	// List<List<Expression>> runs = split(pattern, symbol("..."));
	//
	// if (runs.stream()
	// .map(e -> e.size())
	// .reduce(0, (sum, e) -> sum + e)
	// > candidate.size()) {
	// return false;
	// }
	//
	// if (runs.size() == 1) {
	// return matchesSequence(literals, bindings, runs.get(0),
	// take(runs.get(0).size(), candidate))
	// && matchesSequence(literals, bindings, fillList(candidate.size() -
	// runs.get(0).size(), last(runs.get(0))),
	// takeLast(candidate.size() - runs.get(0).size(), candidate));
	// } else {
	// return matchesSequence(literals, bindings, runs.get(0),
	// take(runs.get(0).size(), candidate))
	// && matchesSequence(literals, bindings, fillList(candidate.size() -
	// runs.get(0).size(), last(runs.get(0))),
	// takeLast(candidate.size() - runs.get(0).size(), candidate))
	// && matchesSequence(literals, bindings, runs.get(1),
	// takeLast(runs.get(1).size(), candidate));
	// }
	// }
	//
	// private static <T> List<List<T>> split(List<T> list, T e) {
	// List<List<T>> lists = new ArrayList<>();
	// return splitRecur(lists, list, e);
	// }
	//
	// private static <T> List<List<T>> splitRecur(List<List<T>> sum, List<T>
	// list, T e) {
	// if (!list.contains(e)) {
	// sum.add(list);
	// return sum;
	// }
	//
	// sum.add(take(list.indexOf(e), list));
	// return splitRecur(sum, list.subList(list.indexOf(e) + 1, list.size()),
	// e);
	// }
	//
	// private static <T> List<T> take(int n, List<T> list) {
	// return list.subList(0, Math.min(n, list.size()));
	// }
	//
	// private static <T> List<T> takeLast(int n, List<T> list) {
	// return list.subList(Math.max(0, list.size() - n), list.size());
	// }
	//
	// private static <T> List<T> fillList(int n, T element) {
	// return IntStream.generate(() -> 0)
	// .limit(n)
	// .mapToObj(a -> element)
	// .collect(Collectors.toList());
	// }
	//
	// private static <T> T last(List<T> list) {
	// return list.get(list.size() - 1);
	// }
	//
	// private static <T> boolean contains(Iterable<T> list, T element) {
	// for (T t : list) {
	// if (element.equals(t)) {
	// return true;
	// }
	// }
	// return
	// }

}
