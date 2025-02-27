/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.preview.rendering;

import de.eldoria.preview.RollingQueue;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class PacketWorker implements Runnable {
    private final Queue<RenderSink> queue = new ArrayDeque<>();
    private final Plugin plugin;
    private final RenderService renderService;
    private boolean active;
    private final AtomicInteger tickChanges = new AtomicInteger();
    private final RollingQueue<Integer> tickUpdates = new RollingQueue<>(1200);
    private final RollingQueue<Integer> queueSize = new RollingQueue<>(1200);
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("SBR-Packet worker");
        return thread;
    });
    private BukkitRunnable flushTask;

    public static PacketWorker create(RenderService renderService, Plugin plugin) {
        PacketWorker packetWorker = new PacketWorker(plugin, renderService);
        RenderTask task = new RenderTask(packetWorker);
        task.runTaskTimerAsynchronously(plugin, 1,1);
        packetWorker.flushTask = task;
        return packetWorker;
    }

    private PacketWorker(Plugin plugin, RenderService renderService) {
        this.plugin = plugin;
        this.renderService = renderService;
    }

    public void flushTickMetrics() {
        tickUpdates.add(tickChanges.getAndSet(0));
        queueSize.add(packetQueueCount());
    }

    @Override
    public void run() {
        if (!claim()) return;
        while (!queue.isEmpty()) {
            var poll = queue.poll();
            tickChanges.addAndGet(poll.sendChanges());
        }
        active = false;
    }

    // There is a minimal chance of a race condition. That's why this method needs to be synchronized
    private synchronized boolean claim() {
        if (active) return false;
        active = true;
        return true;
    }

    /**
     * Queues the render sink to be sent. Will start a worker if now task is running.
     *
     * @param renderSink render sink to queue.
     */
    public void queue(RenderSink renderSink) {
        queue.add(renderSink);
        if (!active) worker.submit(this);
    }


    public void remove(Player player) {
        queue.removeIf(e -> e == null || e.sinkOwner().equals(player.getUniqueId()));
    }

    public int packetQueueCount() {
        // We copy the queue to avoid modification during counting.
        // Synchronizing the queue would stop the worker from working properly
        return new ArrayList<>(queue).stream().filter(Objects::nonNull).mapToInt(RenderSink::size).sum();
    }

    public int size() {
        return queue.size();
    }

    // TODO
//    public String info() {
//        return """
//                Total Sinks Count: %s
//                Active Sinks Count: %s
//                Active Sinks:
//                %s
//                Packets Queued: %s
//                Packets Queued History:
//                %s
//                Average updates last %s ticks: %s
//                Updates per tick:
//                %s
//                """.stripIndent()
//                .formatted(renderService.sinks().size(),
//                        renderService.sinks().stream()
//                                .filter(RenderSink::isActive)
//                                .count(),
//                        renderService.sinks().stream()
//                                .filter(RenderSink::isActive)
//                                .map(RenderSink::info)
//                                .map(t -> t.indent(2))
//                                .collect(Collectors.joining("\n======")),
//                        packetQueueCount(),
//                        Text.inlineEntries(queueSize.values(), 20).indent(2),
//                        tickUpdates.values().size(), tickUpdates.values().stream()
//                                .mapToInt(Integer::intValue)
//                                .average().orElse(0),
//                        Text.inlineEntries(tickUpdates.values(), 20).indent(2));
//    }

    public void shutdown() {
        worker.shutdown();
        flushTask.cancel();
    }

    private static class RenderTask extends BukkitRunnable {
        private final PacketWorker worker;

        private RenderTask(PacketWorker worker) {
            this.worker = worker;
        }

        @Override
        public void run() {
            worker.flushTickMetrics();
        }
    }
}
