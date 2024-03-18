package com.lht.lhtrpc.core.registry;

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

    List<String> data;

}
