package xyz.anthofoxo.aurora;

public class SemVerComparator {

	private SemVerComparator() {
	}

	/**
	 * Compares two SemVer strings (with optional 'v' prefix and pre-release)
	 * Ignores build metadata (+something) Returns negative if v1 < v2, 0 if equal,
	 * positive if v1 > v2
	 */
	public static int compareSemVer(String v1, String v2) {
		// Strip 'v' prefix
		if (v1.startsWith("v") || v1.startsWith("V")) v1 = v1.substring(1);
		if (v2.startsWith("v") || v2.startsWith("V")) v2 = v2.substring(1);

		// Ignore build metadata
		v1 = v1.split("\\+")[0];
		v2 = v2.split("\\+")[0];

		// Split pre-release
		String[] parts1 = v1.split("-", 2);
		String[] parts2 = v2.split("-", 2);

		// Compare numeric version first
		int cmp = compareNumericVersion(parts1[0], parts2[0]);
		if (cmp != 0) return cmp;

		// Both are same numeric version, now compare pre-release
		String pre1 = parts1.length > 1 ? parts1[1] : null;
		String pre2 = parts2.length > 1 ? parts2[1] : null;

		// No pre-release means stable → greater than any pre-release
		if (pre1 == null && pre2 == null) return 0;
		if (pre1 == null) return 1; // v1 > v1-beta
		if (pre2 == null) return -1; // v1-beta < v1

		return comparePreRelease(pre1, pre2);
	}

	private static int compareNumericVersion(String num1, String num2) {
		String[] parts1 = num1.split("\\.");
		String[] parts2 = num2.split("\\.");
		int length = Math.max(parts1.length, parts2.length);

		for (int i = 0; i < length; i++) {
			int n1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
			int n2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
			if (n1 != n2) return n1 - n2;
		}
		return 0;
	}

	private static int comparePreRelease(String pre1, String pre2) {
		String[] identifiers1 = pre1.split("\\.");
		String[] identifiers2 = pre2.split("\\.");
		int length = Math.max(identifiers1.length, identifiers2.length);

		for (int i = 0; i < length; i++) {
			String id1 = i < identifiers1.length ? identifiers1[i] : "";
			String id2 = i < identifiers2.length ? identifiers2[i] : "";

			// Try numeric comparison
			Integer n1 = isNumeric(id1) ? Integer.parseInt(id1) : null;
			Integer n2 = isNumeric(id2) ? Integer.parseInt(id2) : null;

			if (n1 != null && n2 != null) {
				if (!n1.equals(n2)) return n1 - n2;
			} else if (n1 != null) {
				return -1; // numeric < alphanumeric
			} else if (n2 != null) {
				return 1; // alphanumeric > numeric
			} else { // both alphanumeric
				int cmp = id1.compareTo(id2);
				if (cmp != 0) return cmp;
			}
		}
		return 0;
	}

	private static boolean isNumeric(String str) {
		return str.matches("\\d+");
	}
}
