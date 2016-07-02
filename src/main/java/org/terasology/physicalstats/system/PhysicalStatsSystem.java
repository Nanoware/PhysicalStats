/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.physicalstats.system;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.characters.GetMaxSpeedEvent;
import org.terasology.logic.health.BeforeDamagedEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.physicalstats.component.PhysicalStatsComponent;
import org.terasology.physicalstats.event.OnConstitutionChangedEvent;
import org.terasology.registry.In;

@RegisterSystem
public class PhysicalStatsSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(PhysicalStatsSystem.class);

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(PhysicalStatsComponent.class)) {
            if (clientEntity.hasComponent(HealthComponent.class)) {
                HealthComponent h = clientEntity.getComponent(HealthComponent.class);

                if (h.currentHealth == h.maxHealth) {
                    h.maxHealth = clientEntity.getComponent(PhysicalStatsComponent.class).constitution * 10;
                    h.currentHealth = h.maxHealth;
                }
            }
        }

        super.initialise();
    }

    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, PhysicalStatsComponent eq) {
        if (player.hasComponent(HealthComponent.class)) {
            HealthComponent h = player.getComponent(HealthComponent.class);
            h.maxHealth = eq.constitution * 10;
            h.currentHealth = h.maxHealth;
        }
    }

    @ReceiveEvent
    public void onCONChanged(OnConstitutionChangedEvent event, EntityRef player, PhysicalStatsComponent phy) {
        if (player.hasComponent(HealthComponent.class)) {
            HealthComponent h = player.getComponent(HealthComponent.class);
            h.maxHealth = phy.constitution * 10;
        }
    }

    @ReceiveEvent
    public void impactOnPhysicalDamage(BeforeDamagedEvent event, EntityRef damageTarget) {
        if (event.getInstigator().hasComponent(PhysicalStatsComponent.class)) {
            PhysicalStatsComponent phy = event.getInstigator().getComponent(PhysicalStatsComponent.class);
            event.add(phy.strength / 2f);
        }
    }

    @ReceiveEvent
    public void impactOnSpeed(GetMaxSpeedEvent event, EntityRef entit, PhysicalStatsComponent phy) {
        event.add(Math.max(-1, (phy.agility - 10) / 10f));
    }
}