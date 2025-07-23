package com.bocsoft.wwsf.webconsole.dag;

import java.util.Set;

public class DirectedGraphConcise {

	private Set<VertexConcise> vertexs;
	
	private Set<EdgeConcise> edges;


	public Set<EdgeConcise> getEdges() {
		return edges;
	}

	public void setEdges(Set<EdgeConcise> edges) {
		this.edges = edges;
	}

	public Set<VertexConcise> getVertexs() {
		return vertexs;
	}

	public void setVertexs(Set<VertexConcise> vertexs) {
		this.vertexs = vertexs;
	}
	
}
