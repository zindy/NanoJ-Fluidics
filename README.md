# Aim of this NanoJ-Fluidics fork

First, I would like to extend Nano-J Fluidics (NJF) for our own Multi-Modal Imaging experiments, some of which require both pumps and valves. This should be relatively easy to achieve.

Way down the line, I see NJF as a viable solution to using Micro-Manager to control High-Content Screening (HCS) systems that combine microscopy and fluidics (such as the BDPathway 855 or the Cellomics ArrayScan). For now, there is the [HCS Site Generator](https://micro-manager.org/wiki/HCS_Site_Generator) plugin for multiwell plate imaging, but no plugin for liquid dispensing.

## What do I plan to do?

These steps (I think) will let me write NJF protocols that interact with pumps and valves, and then later, let me use NJF with pumps and valves registered as Micro-Manager devices through the Harware Configuration Wizard.

- [ ] Add the concept of valves (TODO refactor Pump to be called fluidicsDevice first)
- [ ] Support multiple devices or hubs in NJF
- [ ] Decouple pump devices defined in NJF and add them to Micro-Manager as a new type of base class fluidics device (sort of like a z-stage + current, max and dead (?) volume).
- [ ] Create some demo Micro-Manager device adapters as a proof of concept
- [ ] Create a Hamilton pump device adapter, possibly for the [Microlab 500 manual](https://www.microlabtech.co.uk/documents/literature/ML%20500%20B-C%20Manual.pdf) using Protocol 1/RNO+
- [ ] Extend the existing [Hamilton MVP](https://github.com/micro-manager/micro-manager/tree/master/DeviceAdapters/HamiltonMVP) device adapter (which also uses Protocol 1/RNO+) to control daisy chains of both valves and *pumps* (up to 16 individual elements)
- [ ] Add wrapper functions in the JAVA / Python SWIG interfaces to access the fluidics devices from JAVA/Python/Beanshell with simplified commands
- [ ] Update NanoJ Fluidics to use these commands

# NanoJ-Fluidics: open-source fluid exchange in microscopy

Note: visit the [**Wiki**][10] or [**Forum**][12] for latest updates.

![][8]

NanoJ-Fluidics is an open-source device, composed of easily accessible LEGO-parts, electronics and labware. It is designed to automate and simplify fluid exchange experiments in microscopy. Check the paper in Nature Communications: [Automating multimodal microscopy with
NanoJ-Fluidics][11].

## It consists of three parts:
+ [LEGO-based, multiplexable and compact syringe pumps][4]
+ [A simple "hack" to enable liquid exchange on cell culture dishes][5]
+ And a comprehensive [electronic][6] and [software][7] control suite to control the pumps.

This [Wiki][10] provides all the information necessary for researchers to reproduce their own systems and start performing fluidic experiments on their microscopes.

## Developers
NanoJ-Fluidics is developed in a collaboration between the [Henriques][1] and [Leterrier][9] laboratories. 

## Developers
NanoJ-Fluidics is developed in a collaboration between the [Henriques][1] and [Leterrier][9] laboratories, with contributions from the community:
  * [Matthew Meyer][0mgem0] (La Jolla Institute of Allergy & Immunology's Microscopy Core): 3D printed syringe pump body, v-slot adaptor, other parts (see [section](https://github.com/HenriquesLab/NanoJ-Fluidics/wiki/Pumpy-3D-Printing-MMeyer)).
  * [Leo Saunders][MySaundersleo] (University of Colorado Denver): 3D printed syringe pump body (see [section](https://github.com/HenriquesLab/NanoJ-Fluidics/wiki/Pumpy-3D-Printing-LSaunders)).

  [1]: http://www.ucl.ac.uk/lmcb/users/ricardo-henriques
  [2]: http://www.ucl.ac.uk/lmcb/
  [3]: http://www.ucl.ac.uk/
  [4]: https://github.com/HenriquesLab/NanoJ-Fluidics/wiki/Pumpy-Home
  [5]: https://github.com/HenriquesLab/NanoJ-Fluidics/wiki/Labware-Home
  [6]: https://github.com/HenriquesLab/NanoJ-Fluidics/wiki/Electronics-Home
  [7]: https://github.com/HenriquesLab/NanoJ-Fluidics/wiki/GUI-Home
  [8]: https://github.com/HenriquesLab/NanoJ-Fluidics/wiki/Files/PedroPumpsSample.png
  [9]: http://www.neurocytolab.org/
  [10]: https://github.com/HenriquesLab/NanoJ-Fluidics/wiki
  [11]: https://doi.org/10.1038/s41467-019-09231-9
  [12]: https://gitter.im/NanoJ-Fluidics
  [3DPrint]: Pumpy-3D-Printing
  [0mgem0]: https://twitter.com/0mgem0
  [MySaundersleo]: https://twitter.com/MySaundersleo
