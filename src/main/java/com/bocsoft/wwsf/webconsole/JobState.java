package com.bocsoft.wwsf.webconsole;

public class JobState {

	/**
	 * job状态
	 */
	public static final int STATE_INIT = 1; // 初始化
	public static final int STATE_RUNNING = 2; // 正在运行
	public static final int STATE_FACTITIOUS_QUIT = 9; // 强制执行结束
	public static final int STATE_QUIT = 10; // 执行结束
	
	/**
	 * task状态
	 */
	public final static int T_STATE_INIT = 11; // 任务初始化
	public final static int T_STATE_READY = 12; // 正在执行
	public final static int T_STATE_RUNNING = 13; // 正在执行
	public final static int T_STATE_ERROR_DELAY = 14; // 任务出错延迟中
	public final static int T_STATE_FAIL = 15; // 失败
	public final static int T_STATE_MANUAL = 16; // 需人工干预处理
	public final static int T_STATE_PERIOD_DELAY = 17; // 任务执行窗口延迟中
	
	public final static int T_STATE_SUCCESS = 20; // 成功,通过
	public final static int T_STATE_ERROR_IGNORE = 21; // 失败,但忽略错误,通过
	public final static int T_STATE_DISABLED = 22; // 无效任务,因周期不满足，条件不满足
	public final static int T_STATE_FACTITIOUS_DISABLED = 23; // 人工将未执行task调整为disable
	public final static int T_STATE_FACTITIOUS_IGNORE = 24; // 由人工干预,将正在处于运行中的task设置为通过
	
	/**
	 * slave节点处理task后的结果返回值，或者task在调度过程中所遇到的异常情况返回值
	 */
	//task正常调度处理后的返回结果
	public final static int RE_SUCCEED    =  0;	//执行成功
	public final static int RE_FAILED     =  1;	//执行失败
	public final static int RE_DATA_ERROR = -1; //数据错误
	//master端调度时，task异常状态情况
	public final static int RE_PLUGIN_NOT_CONFIG = -3;    //插件未找到
	public final static int RE_AGENT_INVALID = -4;        //未找到可调度的slave节点
	public final static int RE_AGENT_PLUGIN_NOFOUND = -5; //未找到插件
	public final static int RE_UNCATCHED_ERROR = -6;      //未捕获的异常处理
	//agent处理时，task异常情况
	public final static int RE_COMMAND_PROCESS_ERROR = -8;    //agent处理命令时错误
	public final static int RE_PLUGIN_LOSED_AT_BEFORE = -9;  //agent接收指令后，执行任务前插件未找到
	public final static int RE_PLUGIN_POOL_LOSED = -10;  //在agent执行任务时插件未找到
	public final static int RE_PLUGIN_LOSED_IN_RUNNING = -11;  //在agent执行任务时插件未找到
	public final static int RE_TASK_REPEAT = -12;  	//重复的任务
	public final static int RE_TASK_THROWABLE = -13;   //执行任务时抛出异常
	public final static int RE_JOB_PAUSED = -14;
	public final static int RE_JOB_LOSED = -15;
	

}
