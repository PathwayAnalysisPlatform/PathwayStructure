source('readSifFunctions.R')

connections <- readFile('../../Java/PathwayQuery/ProteomeGraph.sif')
length(connections)
names(connections)

pdf('barReactionRole.pdf', width=7, height=7)
barplot(c(length(connections[["cn"]]),
          length(connections[["io"]]),
          length(connections[["co"]]),
          length(connections[["ro"]])),
        main="# proteins with reaction rôle", horiz=TRUE,
        names.arg=c('Complex', 'Input', 'Catalyst', 'Regulator'))
dev.off()

max(sapply(connections[["cn"]], length))
max(sapply(connections[["io"]], length))
max(sapply(connections[["co"]], length))
max(sapply(connections[["ro"]], length))

pdf('densityNumberNeighboursOrOutputs.pdf', width=13, height=7)
plot(density(sapply(connections[["cn"]], length)),
     xlim=c(0, 500), ylim=c(0, 0.08),
     xlab='# outputs',
     main='Density of # outputs/complex neighbours per protein')
lines(density(sapply(connections[["io"]], length)),
      col=rgb(1, 0, 1, 0.5))
lines(density(sapply(connections[["co"]], length)),
      col=rgb(0,0,1,0.5))
lines(density(sapply(connections[["ro"]], length)),
      col=rgb(1,0,0,0.5))
legend('topright',
       c('complex (849)', 'input (1019)', 'catalyst (640)', 'regulator (202)'),
       lty=1, col=c('black', 'magenta', 'blue', 'red'))
dev.off()
