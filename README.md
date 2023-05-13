# TakeASeat
A Fabric server-side mod that allows you to sit on a customizable list of block by right-clicking.  
Despite it's friendly name, it probably is one of the fewer mod that allows you to put lots of restrictions on whether player can sit, ironic isn't it?

## Config
The config file is located in `config/takeaseat.json`, editable value as follows:

| Key                        | Description                                                                                                                                                                                                                                                                                                                                                                                                             | Default Value       | Type         |
|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|--------------|
| allowedBlockId             | An array of block id that player can right click and sit on                                                                                                                                                                                                                                                                                                                                                             | []                  | String Array |
| allowedBlockTag            | An array of block tags that player can right click and sit on                                                                                                                                                                                                                                                                                                                                                           | ["stairs", "slabs"] | String array |
| ensurePlayerWontSuffocate  | Do not allow player to sit if there's a suffocatable block on top.<br><br>Note that this only applies to suffocatable block, usually a full<br>1x1 block and doesn't affect other non-suffocatable block like stairs<br>or light block.                                                                                                                                                                                 | true                | boolean      |
| mustBeEmptyHandToSit       | Whether the player must not be holding any item on both hand to sit.                                                                                                                                                                                                                                                                                                                                                    | true                | boolean      |
| blockMustBeLowerThanPlayer | This ensures the block (seat) the player clicked must not be higher<br>than the player by at most 0.5 meter.<br><br>Useful to prevent player from climbing to house ceiling made with stairs.                                                                                                                                                                                                                           | true                | boolean      |
| mustNotBeObstructed        | This further restricts player by ensuring no solid block exist between the<br>player and the seat. (Solid block implies the block must not have a collidable area)<br><br>This is a rather harsh measure and could create false positives,<br>sometimes requiring player to get closer to the seat before being able to sit, but is<br>useful on server that don't want visitors to sit on otherwise inaccessible area. | false               | boolean      |
| maxDistance                | Distance in blocks that the player must get closer to before being able to sit<br><br>Value less than or equals to 0 means there's no max distance.                                                                                                                                                                                                                                                                     | 0                   | double       |

## Bugs/Suggestions
You may open an GitHub issue [here](https://github.com/Kenny-Hui/TakeASeat/issues), please keep things civil :)

## License
This project is licensed under the MIT License.