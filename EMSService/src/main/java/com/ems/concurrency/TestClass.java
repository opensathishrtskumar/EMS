package com.ems.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestClass {

	public static final String DD_MM_YYYY_HH_MM_S = "dd/MM/yyyy HH:mm:s";

	public static void main(String[] args) throws Exception {

		/*
		 * Stream<String> stream = Stream.of("a", "b", "c").filter(element ->
		 * element.contains("b")); Optional<String> anyElement = stream.findAny();
		 * Optional<String> firstElement = stream.findFirst();
		 * System.out.println(anyElement.isPresent());
		 */

		List<Product> productList = new ArrayList<Product>();
		productList.add(new Product(10, "A"));
		productList.add(new Product(10, "B"));
		productList.add(new Product(11, "CB"));

		Map<Integer, List<Product>> collectorMapOfLists = productList.stream()
				.collect(Collectors.groupingBy(Product::getPrice));

		System.out.println(collectorMapOfLists);
	}
}

class Product {

	int price;
	String name;

	public Product() {

	}

	public Product(int price, String name) {
		this.price = price;
		this.name = name;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Product [price=" + price + ", name=" + name + "]";
	}

}
