## Data
load("~/Documents/data/rpkm_Tcell_123samples.Rdata")
load("~/Dropbox/Erik/count_all_batch_times_sample.names_96Samples_noCD8_leiden.Rdata")

# Column Names Data
count.colnames <- colnames(count.all)
rpkm.colnames <- colnames(rpkm)

# Filter column names.
rpkm.filtered.colnames = rpkm.colnames[rpkm.colnames %in% count.colnames]

# Filter data, only select data from rpkm data where the column names match with count.all data.
rpkm.filtered <- rpkm[,rpkm.filtered.colnames]

# Throw away NA's.
na.pos <- which(is.na(rpkm.filtered[,colnames(rpkm.filtered)]) == TRUE)
rpkm.filtered <- rpkm.filtered[-na.pos,]
# Show count.all data
colnames(rpkm.filtered)

curDate <- date()
save.image(file=paste(getwd(),"/Documents/data/rpkm123leidenfiltered ",curDate,".RData",sep=""))
save(rpkm.filtered, file=paste(getwd(),"/Documents/data/rpkm123leidenfiltered ",curDate,".RData",sep=""))
