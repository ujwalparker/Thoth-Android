package com.gnuc.thoth.framework.callbacks;
public interface ThothMessageCallback
{
	void handle(boolean result);
	void send(String message);
}
