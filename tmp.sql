WITH liegenschaft AS 
(
    SELECT 
        geometrie
    FROM 
        live.dm01vch24lv95dliegenschaften_grundstueck AS grundstueck
        LEFT JOIN live.dm01vch24lv95dliegenschaften_liegenschaft AS liegenschaft
        ON liegenschaft.liegenschaft_von = grundstueck.t_id 
    WHERE
        egris_egrid = 'CH367032068145'
)
SELECT 
    *
FROM 
    live.dm01vch24lv95dliegenschaften_projliegenschaft AS projliegenschaft
    LEFT JOIN live.dm01vch24lv95dliegenschaften_projgrundstueck AS projgrundstueck
    ON projliegenschaft.projliegenschaft_von = projgrundstueck.t_id
    LEFT JOIN live.dm01vch24lv95dliegenschaften_lsnachfuehrung AS nachfuehrung
    ON projgrundstueck.entstehung = nachfuehrung.t_id ,
    
    liegenschaft 
WHERE
    ST_Intersects(projliegenschaft.geometrie, liegenschaft.geometrie )
