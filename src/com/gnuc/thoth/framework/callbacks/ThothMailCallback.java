package com.gnuc.thoth.framework.callbacks;

public interface ThothMailCallback
{
	void handle(boolean result,int resultCode);
	void progress(int resultCode,int progressValue);
}
