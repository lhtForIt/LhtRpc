package com.lht.lhtrpc.core.cluster;

import com.lht.lhtrpc.core.api.Router;
import com.lht.lhtrpc.core.meta.InstanceMeta;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 灰度路由
 *
 * @author Leo
 * @date 2024/04/03
 */
@Slf4j
public class GrayRouter implements Router<InstanceMeta> {

    private Random random = new Random();

    @Setter
    private int grayRatio;

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {


        List<InstanceMeta> grays = new ArrayList<>();
        List<InstanceMeta> normals = new ArrayList<>();

        providers.forEach(d->{
            if (d.getParameters().getOrDefault("gray", "false").equals("true")) {
                grays.add(d);
            } else {
                normals.add(d);
            }
        });


        log.debug(" grayRouter grayNodes/normalNodes,grayRatio ===> {}/{},{}", grays.size(), normals.size(), this.grayRatio);

        if (CollectionUtils.isEmpty(grays) || CollectionUtils.isEmpty(normals)) return providers;
        int random = this.random.nextInt(101);

        if (this.grayRatio < 0) {
            return normals;
        } else if (this.grayRatio >= 100) {
            return grays;
        }

        if (random < this.grayRatio) {
            log.debug(" grayRouter grayNodes ===> {}", grays);
            return grays;
        } else {
            log.debug(" grayRouter normalNodes ===> {}", normals);
            return normals;
        }

    }
}
