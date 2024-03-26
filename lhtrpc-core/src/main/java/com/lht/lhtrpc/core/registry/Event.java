package com.lht.lhtrpc.core.registry;

import com.lht.lhtrpc.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author Leo
 * @date 2024/03/18
 */
@Data
@AllArgsConstructor
public class Event {

    List<InstanceMeta> data;

}
