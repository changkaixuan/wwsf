package com.bocsoft.wwsf.webconsole.dag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class DirectedGraph {
	
	private Map<String, Vertex> directedGraph;
	
	public static int SPACE_X = 300;
	public static int SPACE_Y = 100;
	
	public DirectedGraph() {
		directedGraph = new HashMap<String, Vertex>();
	}
	
	/**
	 *根据标签 染色，将整个任务集分割成多个独立任务集，每个独立任务集为一个独立的DAG网
	 */
	public List<Set<Vertex>> dye() {
		Map<String,Set<Vertex>> titleVertex = new HashMap<String,Set<Vertex>>();
		List<Set<String>> group = new ArrayList<Set<String>>();
		List<Set<Vertex>> groupVertex = new ArrayList<Set<Vertex>>();
		for (Vertex v : directedGraph.values()) {
			String ttl = v.getTitle().iterator().next();
			if (!titleVertex.containsKey(ttl)) {
				titleVertex.put(ttl, new HashSet<Vertex>());
			}
			titleVertex.get(ttl).add(v);
			if (group.isEmpty()) {
				Set<String> gt = new HashSet<String>();
				gt.addAll(v.getTitle());
				group.add(gt);
			} else {
				boolean hited = false;
				for (Set<String> vg : group) {
					Set<String> gt = new HashSet<String>();
					gt.addAll(vg);
					gt.retainAll(v.getTitle());
					if (!gt.isEmpty()) {
						vg.addAll(v.getTitle());
						hited = true;
						break;
					}
				}
				if (!hited) {
					Set<String> gt = new HashSet<String>();
					gt.addAll(v.getTitle());
					group.add(gt);
				}
			}
		}
		for (Set<String> gn : group) {
			Set<Vertex> gVertexs = new HashSet<Vertex>();
			for (String vn : gn) {
				gVertexs.addAll(titleVertex.get(vn));
			}
			groupVertex.add(gVertexs);
		}
		int baseMaxy = 0;
		for (Set<Vertex> gv : groupVertex) {
			int cuMaxy = 0;
			Map<String, Set<Vertex>> cengVertexs = new HashMap<String, Set<Vertex>>();
			for (Vertex v : gv) {
				String levelStr = String.valueOf(v.getLevel());
				if (!cengVertexs.containsKey(levelStr)) {
					cengVertexs.put(levelStr, new TreeSet<Vertex>());
				}
				cengVertexs.get(levelStr).add(v);
			}
			for (Set<Vertex> vset : cengVertexs.values()) {
				int cengHigh = 0;
				for (Vertex v : vset) {
					cengHigh ++;
					int xpx = v.getLevel() * SPACE_X;
					v.setXpoint(xpx);
					int ypx = baseMaxy + cengHigh * SPACE_Y;
					v.setYpoint(ypx);
					if (cuMaxy < ypx) cuMaxy = ypx;
				}
			}
			baseMaxy = cuMaxy;
		}
		return groupVertex;
	}
	
	/**
	 * top排序检测闭环
	 * @throws Exception
	 */
	public void toplogicSort() throws Exception {
		int count = 0;
		
		Queue<Vertex> vsQueue = new LinkedList<Vertex>();
		Collection<Vertex> vertexs = directedGraph.values();
		for (Vertex v : vertexs) {
			if (v.getInDegree().intValue() == 0) {
				vsQueue.offer(v);
			}
		}
		Set<String> removedVertexs = new HashSet<String>();
		while (!vsQueue.isEmpty()) {
			Vertex v = vsQueue.poll();
			removedVertexs.add(v.getName());
			count ++;
			for (Edge edge : v.getEdges()) {
				if (edge.getEndVertex().getInDegree().decrementAndGet() == 0) {
					vsQueue.offer(edge.getEndVertex());
				}
			}
		}
		if (count != directedGraph.size()) {
			throw new Exception("任务项实例执行流程存在闭环");
		}
	}
	
	/**
	 * top排序和节点分组打标
	 * @throws Exception
	 */
	public void toplogicSortCeng() throws Exception {
		int count = 0;
		
		Queue<Vertex> vsQueue = new LinkedList<Vertex>();
		Collection<Vertex> vertexs = directedGraph.values();
		int title = 1;
		for (Vertex v : vertexs) {
			if (v.getInDegree().intValue() == 0) {
				v.setLevel(0);
				vsQueue.offer(v);
				v.getTitle().add("S" + title);
				title ++;
			}
		}
		Set<String> removedVertexsName = new HashSet<String>();
		Set<Vertex> removedVertexs = new HashSet<Vertex>();
		while (!vsQueue.isEmpty()) {
			Vertex v = vsQueue.poll();
			removedVertexsName.add(v.getName());
			removedVertexs.add(v);
			count ++;
			for (Edge edge : v.getEdges()) {
				edge.getEndVertex().getTitle().addAll(v.getTitle());
				if (edge.getEndVertex().getLevel() < 0) {
					edge.getEndVertex().setLevel(v.getLevel()+1);
				}
				if (edge.getEndVertex().getInDegree().decrementAndGet() == 0) {
					vsQueue.offer(edge.getEndVertex());
				}
			}
		}
		Set<String> circle = new HashSet<String>(directedGraph.keySet());
		circle.removeAll(removedVertexsName);
		if (count != directedGraph.size()) {
			throw new Exception("任务项实例执行流程存在闭环");
		}
	}
	
	public Map<String, Vertex> getDirectedGraph() {
		return directedGraph;
	}

}
