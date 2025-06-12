package dev.necr0manthre.innotournament.util;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class ServerBoundObjManager<T> extends WeakHashMap<MinecraftServer, T> {
	private final ReferenceQueue<MinecraftServer> queue = new ReferenceQueue<>();
	private final Map<WeakReference<MinecraftServer>, T> refMap = new HashMap<>();

	@Override
	public T put(MinecraftServer key, T value) {
		WeakReference<MinecraftServer> ref = new WeakReference<>(key, queue);
		refMap.put(ref, value);
		return super.put(key, value);
	}

	public void processQueue() {
		Reference<? extends MinecraftServer> ref;
		while ((ref = queue.poll()) != null) {
			T value = refMap.remove(ref);
			if (value instanceof Removable) {
				((Removable) value).onRemove();
			}
		}
	}

	public void serverTick(MinecraftServer server){
		processQueue();
	}
	TickEvent.Server serverTick = this::serverTick;
	public void register(){
		TickEvent.SERVER_POST.register(serverTick);
	}
	public void unregister(){
		TickEvent.SERVER_POST.unregister(serverTick);
	}

	public interface Removable {
		void onRemove();
	}
}
