package util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utility {

	//
	// metodi per creare e valorizzare un Array
	//

	public static <T> T[] array(T ... t) {
		return t;
	}

	public static <T> T first(T[] t) {
		return t[0];
	}

	public static <T> T last(T[] t) {
		return t[t.length-1];
	}

	public static <T> T pick(int i, T ... t) {
		return t[i];
	}

	public static <T> T pick1(int i, T ... t) {
		return t[i-1];
	}


	//
	// metodi per creare e valorizzare una List
	//

	public static <T> List<T> list(T ... objects) {
		return adds(new ArrayList<T>(), objects);
	}

	public static <T> List<T> adds(List<T> list, T ... objects) {
		for (T o: objects) list.add(o);
		return list;
	}

	public static <T> T first(List <T> list) {
		return list.get(0);
	}

	public static <T> T last(List <T> list) {
		return list.get(list.size()-1);
	}

	public static <T> boolean hasLen0(List list) {
		return list == null || list.size() == 0;
	}


	//
	// metodi per creare e valorizzare una Map
	//

	public static Map map(Object ... objects) {
		return puts(new LinkedHashMap(), objects);
	}

	public static Map puts(Map object, Object ... objects) {
		for (int i=0, e=objects.length-1; i<e; i+=2) {
			object.put(objects[i], objects[i+1]);
		}
		return object;
	}

	//
	// metodi per creare e valorizzare un Set
	//

	public static <T> Set<T> set(T ... objects) {
		return adds(new LinkedHashSet(), objects);
	}

	public static <T> Set<T> adds(Set<T> set, T ... objects) {
		for (T o: objects) set.add(o);
		return set;
	}
	
	/*
	public static <T> Set<T> set2(T ... ts) {
		//return new LinkedHashSet(List.of(ts)); // immutable!
		return new LinkedHashSet(asList(ts)); // mutable!
	}
	*/
}
