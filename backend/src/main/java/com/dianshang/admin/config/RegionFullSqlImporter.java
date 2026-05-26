package com.dianshang.admin.config;

import com.dianshang.admin.system.repository.RegionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 国标区划仅首次导入（约 3300+ 条）。直筒子市（东莞/中山/儋州）在源数据中 code 重复，
 * 已在 region-full.sql 中去掉与市级同 code 的区县行，避免主键冲突。
 */
@Component
@Order(20)
public class RegionFullSqlImporter implements CommandLineRunner {

    private final RegionRepository regionRepository;
    private final DataSource dataSource;

    public RegionFullSqlImporter(RegionRepository regionRepository, DataSource dataSource) {
        this.regionRepository = regionRepository;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        if (regionRepository.count() > 500) {
            return;
        }
        var populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("region/region-full.sql"));
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}
