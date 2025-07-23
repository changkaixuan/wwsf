package com.bocsoft.wwsf.webconsole.dag;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Vertex implements Comparable<Vertex>{
	//入度
	private AtomicInteger inDegree;
	//节点名
	private String name;
	//出线
	private List<Edge> edges;
	//层级
	private int level = -1;
	//标签
	private Set<String> title;
	
	private int xpoint = 0;
	
	private int ypoint = 0;
	
	public Vertex(String vname) {
		this(vname, 0);
	}
	
	public Vertex(String vname, int inDegree) {
		this.name = vname;
		this.inDegree = new AtomicInteger(inDegree);
		this.edges = new LinkedList<Edge>();
		this.title = new HashSet<String>();
	}

	public AtomicInteger getInDegree() {
		return inDegree;
	}

	public void setInDegree(AtomicInteger inDegree) {
		this.inDegree = inDegree;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Set<String> getTitle() {
		return title;
	}

	public void setTitle(Set<String> title) {
		this.title = title;
	}

	public String toString() {
		return name + "(" + xpoint + "," + ypoint + ")";
	}

	public int getXpoint() {
		return xpoint;
	}

	public void setXpoint(int xpoint) {
		this.xpoint = xpoint;
	}

	public int getYpoint() {
		return ypoint;
	}

	public void setYpoint(int ypoint) {
		this.ypoint = ypoint;
	}

	public int compareTo(Vertex o) {
		return getName().compareTo(o.getName());
	}

}
