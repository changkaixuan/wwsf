package com.bocsoft.wwsf.webconsole.dag;

public class Edge {

	private Vertex endVertex;

	public Edge(Vertex endVertex) {
		this.endVertex = endVertex;
	}
	
	public Vertex getEndVertex() {
		return endVertex;
	}

	public void setEndVertex(Vertex endVertex) {
		this.endVertex = endVertex;
	}

	public String toString() {
		return endVertex.getName();
	}
	
}
