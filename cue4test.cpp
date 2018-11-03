#include<iostream>
#include<sstream>
#include<cstdlib>
#include<math.h>
#include<unistd.h>
#include<fstream>
#include<iomanip>
using namespace std;

class PhysData          //all physical parameters
{
 public:
 static const int steps;      // max number of steps taken in single trial
 static const float stepsize; // size of a single step
 int stepcount;               // current number of steps
 static const int diameter;   // diameter of arena
 static const int radius;     // radius of arena (d/2, included for convenience)
 static const int maxcues;    // maximum number of visual cues 

 //inline functions
 double deg_to_rad(int angle) // convert degree to radian
 {
  return angle*3.14159/180;
 }

 int rad_to_deg(double angle)
 {
  return (int)(angle*180/3.14159);
 }
};

const float PhysData::stepsize = 1.5; // single step size ~ 1.414 (sqrt(2))
const int PhysData::steps = 60; // 30fps * 60s

const int PhysData::diameter = 15; //10s to cross arena* 30 steps/s * 1.5 cells/step
const int PhysData::radius = (PhysData::diameter)/2;

const int PhysData::maxcues = 10; // just an upper bound to the number of cues

class CueData: public PhysData 
{
 public:
 int number;      // cue ID number
 int x;           // x - coordinate
 int y;           // y - coordinate
 double platvect; // direction of platform from cue
 double platdist; // distance of platform from cue
 double randvect[diameter][diameter]; // random vector indicated by the cue
 float confidence[diameter][diameter]; // weight of each cue, perceived from a cell

 void platform_relative(int px, int py)
 { 
  double celldist;
  // set platform distance and platform angle from the cue
  // platform distance the is cartesian distance between platform and cue
  platdist = sqrt(pow(px-x,2) + pow(py-y,2));
  // inverse tangent of (Yplat - y)/(Xplat - x), -pi to +pi radian
  platvect = atan2(py-y, px-x);
    if(platvect < 0)
     platvect += deg_to_rad(360); // convert negative angles to pi+

  // initialise confidence from each cell to be (1/Dplat)*(1/Dcell)
  for(int i=0; i<diameter; i++)
   for(int j=0; j<diameter; j++)
   {
    celldist = sqrt(pow(i-x,2) + pow(j-y,2));
    confidence[i][j] = 1/(platdist*celldist);
    randvect[i][j] = deg_to_rad(rand()%360);
   }
 }

 void print_cue();
};
 
void CueData::print_cue()
{
 cout<<"Cue Number "<<number<<endl<<endl;
 double centdist; 

 // print the confidence and random vector arrays
 cout<<"Confidence:\n";
 for(int i=0; i<diameter; i++)
 {
  for(int j=0; j<diameter; j++)
  {
   centdist = sqrt(pow(radius-i,2) + pow(radius-j,2)); // distance from C = (r,r)
   if(centdist > radius) 
    cout<<"\t";
   else cout<<setprecision(3)<<confidence[i][j]<<"\t";
  }
  cout<<endl<<endl<<endl;
 }

 cout<<"Pointer Vector:\n";
 for(int i=14; i>=0; i--)
 {
  for(int j=0; j<diameter; j++)
  {
   centdist = sqrt(pow(radius-i,2) + pow(radius-j,2)); // distance from C = (r,r)
   if(centdist > radius) 
    cout<<"\t";
   else if(i==10 && j==10)
    cout<<"PLAT\t";
   else cout<<rad_to_deg(randvect[i][j])<<"\t";
  }
  cout<<endl<<endl<<endl;
 }
}

class Rat               // the moving particle
{
 public:
 float xpos;                  // x-coordinate of particle
 float ypos;                  // y-coordinate of particle  
 double theta;                // direction of motion, in radian
};


class ArenaCell        // characteristics of arena cell for mapping
{
 public:
 int visited;              // no of visits
 double dirvect;           // direction vector based on previous memory
 double centerangle;       // angle of cell to CoM
 float comwt;              // weight based on previous deflection from CoM
 float platwt;             // based inverse of platform-CoM disance
};

class Arena: public PhysData // the water maze, and related operations
{
 public:
 ArenaCell arena[diameter][diameter];     // the maze where the trial takes place
 ArenaCell memarena[diameter][diameter];  // the arena stored in memory
 bool isfirsttrial;                     // to check if memory initialisation needed

 int center[2];       // x, y coordinate of the center of the arena
 int startcell[2];    // x, y coordinate of the start cell
 int platform[2];     // x, y coordinate of the platform
 int platquad[2];     // start and end angles of platform quadrant in degrees
 int centermass[2];   // x, y coordinate of the CoM of the search
 int path[steps][2];  // x, y coordinate of each step of the search path, in order
 double invdist;      // inverse of platform - CoM distance

 void get_parameters();       // create and initialize an arena for the trial
 void randomize_start();      // get a random start cell position
 void average_direction();    // after simulation, average out direction vectors
 void center_of_mass();       // find CoM after a search
 void angle_to_center();      // find angle from cell to CoM;
 void compute_weight();       // compute Wcom
 void compute_distance();     // compute Wplat 
 void update_memory();        // update results to memory
 void print_arena(); // print all trial results
 void print_stored();         // print data stored in the memory

 double center_dist(int x, int y) // distance of (x,y) from center of the arena
 {
  return sqrt(pow(center[0]-x,2) + pow(center[1]-y,2)); 
 }
};

void Arena::get_parameters()
{
 center[0] = center[1] = radius;          // set location of maze center

 /* 
  set platform location to 3/4th of the maze radius from the cente, 
  along the bisector of the first quadrant
 */
 double px, py, prad, pangle;                    
 platquad[0] = 0, platquad[1] = 90;               
 prad = 0.75*radius;                              
 pangle = deg_to_rad((platquad[1]-platquad[0])/2);
 platform[0] = int(center[0] + prad*cos(pangle));  //x = x0 + r*cos(p)
 platform[1] = int(center[0] + prad*sin(pangle));  //y = y0 + r*sin(p)
 
 // initialise visits to each cell of the arena to 0, invalid direction vectors 
 for(int i=0; i<diameter; i++)
  for(int j=0; j<diameter; j++)
  {
   arena[i][j].visited = 0; 
   arena[i][j].dirvect = -1;
  }

 // initialise each path coordinate to 0 (invalid coordinates, redundant step)
 for(int i=0; i<steps; i++)
  path[i][0] = path[i][1] = 0;

 memarena[platform[0]][platform[1]].comwt = 1; // to prevent avoidance of cell

 /*
  if it's the first trial, initialise the arena in memory,
  by setting visits to each cell of the arena to 0, invalid direction vectors
 */
 if(isfirsttrial)          
 {
  for(int i=0; i<diameter; i++)
   for(int j=0; j<diameter; j++)
   {
    memarena[i][j].visited = 0;
    memarena[i][j].dirvect = -1;
    memarena[i][j].comwt = memarena[i][j].platwt = 0;
   }
   memarena[platform[0]][platform[1]].comwt = 1; // to prevent avoidance of cell
 }
}

void Arena::randomize_start()
{
 /*
  randomize the angle of the start platform relative to the horizontal,
  ensuring that it does not lie between the angles of the patform quadrant
 */

 double sx, sy, sangle, temp; 
 do
 {
  temp = rand()%360; // randomize angle
 }
 while(temp>platquad[0] && temp<platquad[1]); // until not in platform quadrant
 
 /*
  set the coordinates of the start position at the perimeter of the arena,
  at a valid (non-platform quadrant) angle
 */ 
 sangle = deg_to_rad(temp);
 startcell[0] = int(center[0] + radius*cos(sangle)); 
 startcell[1] = int(center[0] + radius*sin(sangle)); 
}

void Arena::average_direction()
{
 /*
  compute the average direction-of-motion vector for each cell of the search path,
  by dividing the summed-up vector by the number of visits
 */
 for(int i=0; i<diameter; i++)
  for(int j=0; j<diameter; j++) //sum of vectors per visit present in each cell
   if(arena[i][j].visited && arena[i][j].dirvect != -1) // visited, has vector
    arena[i][j].dirvect /= arena[i][j].visited; //divide by visits
}

void Arena::center_of_mass() 
{
 /*
  compute the x-y coordinates of the center of mass relative arena[0][0], 
  by assuming the map of visits to be a plane lamina of discrete masses,
  where Xcom = summation MiXi/ summation Mi, Ycom = summation MiYi/ summation Yi
 */

 int i, j;
 double xcom, ycom, totalweight;
 xcom = ycom = totalweight = 0;
 for(i=0; i<diameter; i++)
  for(j=0; j<diameter; j++)
   if(center_dist(i,j) <= radius)
   {
    totalweight += arena[i][j].visited; // summation Mi
    xcom += i*arena[i][j].visited; // summation MiXi
    ycom += j*arena[i][j].visited;  // summation MiYi
   }

 // assign the coordinates to center of mass
 centermass[0] = int(xcom/totalweight);
 centermass[1] = int(ycom/totalweight);
}

void Arena::angle_to_center()
{
 /*
  compute the angle to the center of mass from each cell of the search path,
  where Tcom = arctan((Ycom-y)/(Xcom-x)), 
  for cell coordinates (x,y) and center of mass coordinates (Xcom, Ycom)
 */
 int i, j;
 for(i=0; i<diameter; i++)
  for(j=0; j<diameter; j++)
   if(center_dist(i,j)<=radius && arena[i][j].visited) // valid cell visited
   {
    // inverse tangent of (Ycom - Yi)/(Xcom - Xj), -180 to +180 degrees
    arena[i][j].centerangle = atan2(centermass[1]-j, centermass[0]-i);
    if(arena[i][j].centerangle < 0)
     arena[i][j].centerangle += deg_to_rad(360); // convert negative angles to 180+
   } 
}

void Arena::compute_weight()
{
 /*
  compute Wcom for each cell in the path with a direction vector,
  where Wcom = 1-0.1*(floor(d/18)), where d is modulus of the deflection,
  i.e. the difference between angle to the center of mass and average direction, 
 */
 int i, j, offset;
 for(i=0; i<diameter; i++)
  for(j=0; j<diameter; j++)
   if(arena[i][j].visited && arena[i][j].dirvect != -1) // valid cell visited
   {
    offset = rad_to_deg(arena[i][j].centerangle - arena[i][j].dirvect);
    if(offset < 0) 
     offset = -offset; // modulus of angle distance
    if(offset > 360-offset)
     offset = 360 - offset;  // the smaller distance is considered 
    arena[i][j].comwt = 1 - (offset/18)*0.1;
   }
}

void Arena::compute_distance()
{
 /*
  compute Wplat for the trial,
  where Wplat = 1/D is the inverse of the cartesian distance,
  D = sqrt((Xp-Xcom)^2 + (Yp-Ycom)^2), between the platform and center of mass
 */
 double dist;
 dist = sqrt(pow(platform[0]-centermass[0],2) + pow(platform[1]-centermass[1],2));
 if(dist < 1) dist = 1; // make the inverse valid, and max reinforcement for < 1
 invdist = 1/dist;
 
 // assign the value of Wplat to each cell
 for(int i=0; i<diameter; i++)
  for(int j=0; j<diameter; j++)
   if(center_dist(i,j)<=radius && arena[i][j].visited) 
   {
    arena[i][j].platwt = invdist;
   }
}
  
void Arena::update_memory() 
{                        
 /*
  update the stored data with the results of the current trial, 
  by averaging the parameters (average direction vector, Wcom, Wplat) for each cell.
  If there is no data in that memory cell, assign the trial data to that cell
 */
 for(int i=0; i<diameter; i++)
  for(int j=0; j<diameter; j++)
   if(arena[i][j].visited)
   {
    if(!memarena[i][j].visited) // first ever visit to the cell
    {
     memarena[i][j] = arena[i][j]; // assign current trial values
     continue;
    }
    /* 
     else, cell has been visited, 
     and  memarena or arena or both may not have direction vectors in that cell,
     if arena has no direction vector (last search step/ platform), no change,
     else if memarena has no direction vector, assign arena vector and CoM weight,
     else if both have them, then average them out and store to memarena,
     else if neither have them, do nothing,
     and compute average for platform weight and store to memarena
    */ 

    if(arena[i][j].dirvect != -1) // cell in current trial has direction vector
    {
     if(memarena[i][j].dirvect == -1) // visited, but no stored vector
     {
      memarena[i][j].dirvect = arena[i][j].dirvect; // assign vector 
      memarena[i][j].comwt = arena[i][j].comwt;    // and CoM weight
     }
     else       // visited, and direction vector exists
     {
      // compute the average of average angles and CoM weights
      memarena[i][j].dirvect *= memarena[i][j].visited;
      memarena[i][j].dirvect += arena[i][j].dirvect*arena[i][j].visited; 
      
      memarena[i][j].comwt *= memarena[i][j].visited; 
      memarena[i][j].comwt += arena[i][j].comwt*arena[i][j].visited;

      memarena[i][j].dirvect /= memarena[i][j].visited + arena[i][j].visited;
      memarena[i][j].comwt /= memarena[i][j].visited + arena[i][j].visited;      
     }
    }
    // else, no direction vector in this trial, so no update

    // average out platform weights
    memarena[i][j].platwt *= memarena[i][j].visited;
    memarena[i][j].platwt += arena[i][j].platwt*arena[i][j].visited;

    // sum up the visits and divide by the total number of visits for average    
    memarena[i][j].visited += arena[i][j].visited;   
    memarena[i][j].platwt /= memarena[i][j].visited;
   }    
   // else, cell not visited in this trial, so no memory change
}

void Arena::print_arena()
{ 
 //print all experiment parameters and current trial results

 int i, j;

 cout<<"Start location: ";
 cout<<"("<<startcell[0]<<", "<<startcell[1]<<")\n";

 cout<<"Platform location: ";
 cout<<"("<<platform[0]<<", "<<platform[1]<<")\n";
/*
 cout<<"Path:\n";
 for(i=0; i<stepcount; i++)
  cout<<"("<<path[i][0]<<", "<<path[i][1]<<")\n";
 cout<<endl;
*/
 cout<<"Heat map of visits:\n\n";
 for(i=0; i<diameter; i++)
 {
  for(j=0; j<diameter; j++)
  {
   if(center_dist(i,j) > radius)
    cout<<"\t";
   else
    cout<<arena[i][j].visited<<"\t";
  }
  cout<<endl<<endl<<endl;
 }

 cout<<"\n\nAverage direction vector:\n\n";
 for(i=0; i<diameter; i++)
 {
  for(j=0; j<diameter; j++)
  {
   if(center_dist(i,j) > radius)
    cout<<"\t";
   else if(arena[i][j].visited && arena[i][j].dirvect != -1)
    cout<<rad_to_deg(arena[i][j].dirvect)<<"\t";
   else
    cout<<"-1"<<"\t";
  }
  cout<<endl<<endl<<endl;
 }

 cout<<"\nCenter of Mass: ("<<centermass[0]<<", "<<centermass[1]<<")\n\n";
/*
 cout<<"\n\nAngle to Center of Mass:\n\n";
 for(i=0; i<diameter; i++)
 {
  for(j=0; j<diameter; j++)
  {
   if(center_dist(i,j) > radius)
    cout<<"\t";
   else if(arena[i][j].visited)
    cout<<rad_to_deg(arena[i][j].centerangle)<<"\t";
   else
    cout<<"-1"<<"\t";
  }
  cout<<endl<<endl<<endl;
 }
*/
 cout<<"\n\nStored Weight:\n\n";
 for(i=0; i<diameter; i++)
 {
  for(j=0; j<diameter; j++)
  {
   if(center_dist(i,j) > radius)
    cout<<"\t";
   else if(arena[i][j].visited)
    cout<<arena[i][j].comwt<<"\t";
   else
    cout<<"-1"<<"\t";
  }
  cout<<endl<<endl<<endl;
 }

 cout<<"Distance of CoM to platform: "<<1/invdist<<"\t\t";
 cout<<"Inverse distance: "<<invdist<<endl;
}

void Arena::print_stored()
{
 // print the data stored in memory

 int i, j, total = 0;

 cout<<"\n\nVisits in memory:\n\n";
 for(i=0; i<diameter; i++)
 {
  for(j=0; j<diameter; j++)
  {
   if(center_dist(i,j) > radius)
    cout<<"\t";
   else
    cout<<memarena[i][j].visited<<"\t";
  }
  cout<<endl<<endl<<endl;
 }

 cout<<"\n\nAverage direction vector in Memory:\n\n";
 for(i=0; i<diameter; i++)
 {
  for(j=0; j<diameter; j++)
  {
   if(center_dist(i,j) > radius)
    cout<<"\t";
   else if(memarena[i][j].visited && memarena[i][j].dirvect != -1)
    cout<<rad_to_deg(memarena[i][j].dirvect)<<"\t";
   else
    cout<<"-1"<<"\t";
  }
  cout<<endl<<endl<<endl;
 }

 cout<<"\n\nStored Weight in Memory:\n\n";
 for(i=0; i<diameter; i++)
 {
  for(j=0; j<diameter; j++)
  {
   if(center_dist(i,j) > radius)
    cout<<"\t";
   else if(memarena[i][j].visited)
    cout<<setprecision(3)<<memarena[i][j].comwt<<"\t";
   else
    cout<<"-1"<<"\t";
  }
  cout<<endl<<endl<<endl;
 }

 cout<<"\n\nInverse Distance Weight in Memory:\n\n";
 for(i=0; i<diameter; i++)
 {
  for(j=0; j<diameter; j++)
  {
   if(center_dist(i,j) > radius)
    cout<<"\t";
   else if(memarena[i][j].visited)
    cout<<setprecision(3)<<memarena[i][j].platwt<<"\t";
   else
    cout<<"-1"<<"\t";
  }
  cout<<endl<<endl<<endl;
 }

 cout<<"\n\nProduct of Weights in Memory:\n\n";
 for(i=0; i<diameter; i++)
 {
  for(j=0; j<diameter; j++)
  {
   if(center_dist(i,j) > radius)
    cout<<"\t";
   else if(memarena[i][j].visited)
    cout<<setprecision(3)<<memarena[i][j].comwt*memarena[i][j].platwt<<"\t";
   else
    cout<<"-1"<<"\t";
  }
  cout<<endl<<endl<<endl;
 }

 cout<<"Total visits: ";
 for(i=0; i<diameter; i++)
 {
  for(j=0; j<diameter; j++)
  {
   total += memarena[i][j].visited;
  }
 }
 cout<<total<<endl;
}  

class Sim: public Rat, public Arena // a rat, the maze, and their interactions 
{
 //contains Rat and Arena parameters and methods
 public:
 int idnumber;                 // to uniquely identify each rat 
 int curx, cury;               // current particle postition
 int numcues;                  // the number of visual cues
 CueData viscues[maxcues];     // the visual cues for the rat 
 int successes;         // number of successful trials 

 void initialise();         // set up a simulation
 void next_step();          // take a step into a new cell, update values
 bool monte_carlo_search(); // the search procedure
 void update_cues();        // update the direction indicated and confidence
 void run_simulation();     // run the search and update parameters for a trial
 void store_to_file();      // store data to file
 void read_from_file(int number); // read data from file

 void put_cues()
 {
  //for(int i=0; i<numcues; i++)
  // viscues[i].print_cue();
  if(numcues)
   viscues[0].print_cue(); // all identical, anyway
 }

 void print_results()
 {
  //print_arena();
  print_stored();
  put_cues();

  for(int i=14; i>=0; i--)
  {
   for(int j=0; j<15; j++)
   {
    if(center_dist(i,j) > radius)
     cout<<"\t";
    else if(i==platform[0] && j==platform[1])
     cout<<"PLAT\t";
    else if(memarena[i][j].visited && memarena[i][j].dirvect != -1)
     cout<<rad_to_deg(memarena[i][j].dirvect)<<"\t";
    else
     cout<<"-1"<<"\t";
   }
   cout<<endl<<endl<<endl;
  }  
 }
};

void Sim::initialise()
{
 idnumber = 31;

 get_parameters();

 if(isfirsttrial)
 {
  successes = 0;

  cout<<"Enter number of cues: ";
  cin>>numcues;
  if(numcues)
  {
   for(int i=0; i<diameter; i++)
   {
    for(int j=0; j<diameter; j++)
     if(center_dist(i,j) > radius)
      cout<<"\t";
     else
     {
      if(i==platform[0] && j == platform[1])
       cout<<"PLAT\t";
      else
       cout<<"("<<i<<","<<j<<")\t";
     }
    cout<<endl<<endl<<endl;
   }
   
   for(int i=0; i<numcues; i++)
   {
    viscues[i].number = i;
    cout<<"Enter the coordinates of cue "<<i<<": ";
    cin>>viscues[i].x>>viscues[i].y;
    viscues[i].platform_relative(platform[0], platform[1]);
   }
  }
 }
}


void Sim::next_step()
{
 /*
  take a step in the arena, based on the angle in memory and a random angle,
  where Tnext = Tmem(WcomWplat) + Trand(1-WcomWplat)
 */
 float x, y, wmtheta = 0, score = 0, tscore;    
 int i, best = -1, cuestep[numcues][2], nx, ny; // index of best of the cue results

 theta = deg_to_rad(rand()%360); // Monte Carlo step

 for(i=0; i<numcues; i++)
 {
  cuestep[i][0] = int(xpos + stepsize*cos(viscues[i].randvect[curx][cury]));
  cuestep[i][1] = int(ypos + stepsize*sin(viscues[i].randvect[curx][cury]));
 }
 for(i=0; i<numcues; i++)   // find best cell of ones cues point to
 {
  nx = cuestep[i][0], ny = cuestep[i][1];
  if(center_dist(nx, ny) <= radius)
  {
   tscore = memarena[nx][ny].comwt * memarena[nx][ny].platwt;
   tscore *= viscues[i].confidence[nx][ny];
   if(score < tscore)
   {
    score = tscore;         // contains best score after loop
    best = i;               // contains best index after loop
   }
  }
 }

 if(memarena[curx][cury].visited) //visited from memory, use weighted result
 {
  tscore = memarena[curx][cury].comwt*memarena[curx][cury].platwt;
  wmtheta = memarena[curx][cury].dirvect*tscore;
  theta = theta*(1-tscore) + wmtheta; // Tr(1-MmMp) + TmMmMp
 }
 x = xpos + stepsize*cos(theta);
 y = ypos + stepsize*sin(theta);

 while(center_dist(x, y) > radius) // while out of bounds (happens at edges)
 {
  theta = deg_to_rad(rand()%360); // choose a random angle till valid
  x = xpos + stepsize*cos(theta);
  y = ypos + stepsize*sin(theta);
 }

 if(numcues)
 {
  if(best != -1)
  {
   tscore = memarena[(int)x][(int)y].comwt*memarena[(int)x][(int)y].platwt;
   tscore /= numcues; // normalise??? 
   if(score > tscore)          // assign best cue value if better than random
   {
    theta = viscues[best].randvect[curx][cury];
    x = cuestep[best][0], y = cuestep[best][1];
   }
  } 
 }
 

 /*
  once a valid angle is obtained, update the average direction vector for the cell, 
  then move to the new cell and increment its number of visits
 */
 if(arena[curx][cury].dirvect == -1) // cell has no direction vector
  arena[curx][cury].dirvect = theta;
 else                               // has existing direction vector
  arena[curx][cury].dirvect += theta;

 xpos = x; // update coordinates to new cell
 ypos = y;
 curx = int(xpos);
 cury = int(ypos);

 arena[curx][cury].visited += 1; // new cell is visited
}


bool Sim::monte_carlo_search() 
{
 /*
  take steps and store to path,
  till either platform is encountered or maximum number of steps are taken
 */
 randomize_start();
 xpos = curx = startcell[0], ypos = cury = startcell[1]; // set start position
 for(stepcount=0; stepcount<steps; stepcount++)
 {
  next_step();                  // take a step and update the arena
  path[stepcount][0] = curx;
  path[stepcount][1] = cury;
  if(curx==platform[0] && cury==platform[1])  // platform encountered
   return true;
 }
 return false; // search ends unsuccessfully
}

void Sim::update_cues()
{
 /*
  modify the direction pointed to by and confidence in a cue, cell-by-cell,
  by computing weight similar to comwt for confidence offset dCi,
  and computing Ci = (Ci + dCi)/(summation Ci+dCi) to normalise,
  and modifying direction Di as DiCi + Tavg(1-Ci), 
  updating Ci and Di independently for each cue i, in each vell visited in the trial
 */
 int i, j, k, offset;
 float totalconf;
 CueData oldcues[maxcues];

 for(i=0; i<numcues; i++)
  oldcues[i] = viscues[i];       // store old values to use in modifying

 // update confidence based on offset between average direction and cue vector
 for(i=0; i<diameter; i++)
  for(j=0; j<diameter; j++)
   if(arena[i][j].visited && arena[i][j].dirvect != -1) // valid cell visited
   {
    for(k=0; k<numcues; k++) // for each cue k, compute new confidence Ck+dCk
    { 
     offset = rad_to_deg(oldcues[k].randvect[i][j] - arena[i][j].dirvect);
     if(offset < 0) 
      offset = -offset; // modulus of angle distance
     if(offset > 360-offset)
      offset = 360 - offset;  // the smaller distance is considered 
     viscues[k].confidence[i][j] += 1 - (offset/18)*0.1;
    }
   }
 // divide confidence of each cue by total confidence of all cues, for a cell
 for(i=0; i<diameter; i++)
  for(j=0; j<diameter; j++)
   if(arena[i][j].visited && arena[i][j].dirvect != -1)  // for each valid cell
   { 
    totalconf = 0;
    for(k=0; k<numcues; k++)
     totalconf += viscues[k].confidence[i][j]; // find summation Ck+dCk
    if(totalconf)
     for(k=0; k<numcues; k++)
       viscues[k].confidence[i][j] /= totalconf; // divide each Ck+dCk by summation 
   }
 // modify direction pointed to based on offset from average, and confidence
 for(i=0; i<diameter; i++)
  for(j=0; j<diameter; j++)
   if(arena[i][j].visited && arena[i][j].dirvect != -1) // valid cell visited
   {
    for(k=0; k<numcues; k++) // for each cue, compute new direction 
    {
     totalconf = oldcues[k].confidence[i][j];
     viscues[k].randvect[i][j] *= totalconf; 
     viscues[k].randvect[i][j] += arena[i][j].dirvect*(1-totalconf);
    }
   }
}

void Sim::run_simulation()
{
 initialise();            // create new arena
 if(monte_carlo_search()) // perform the search
 {
  successes++;           // if successful, increment total number of successes
 }
 average_direction();     // average out all direction vectors
 center_of_mass();        // compute CoM
 angle_to_center();       // compute angle of cell to CoM
 compute_weight();        // compute weight to store
 compute_distance();      // compute inverse distance of CoM from platform
 update_memory();         // update trial results to stored arena
 update_cues();
 //print_results();
}

void Sim::store_to_file()
{
 // store the object details to file
 ostringstream os;
 os<<"rat_"<<idnumber;
 string filename = os.str();
 const char*  fstr = filename.c_str();
 ofstream out(fstr, ios::out|ios::binary|ios::trunc);
 if(!out)
 {
  cout<<"Could not open file\n";
  return;
 }
 out.write((char*)this, sizeof(Sim));
 out.close();
}

void Sim::read_from_file(int number)
{
 // read object details from file
 ostringstream os;
 os<<"rat_"<<number;
 string filename = os.str();
 const char*  fstr = filename.c_str();
 ifstream in(fstr, ios::in|ios::binary); 
 if(!in)
 {
  cout<<"Could not open file\n";
  isfirsttrial = true;
  return;
 }
 in.read((char*)this, sizeof(Sim)); // read into object
 in.close();
 isfirsttrial = false;
}


int main()
{
 sleep(3);                     // ensure new RNG seed
 srand(time(NULL));            // get RNG seed
 bool next = 0;
 Sim test;
 for(int i=1; i<=100000; i++)
 {
  test.read_from_file(31);
  test.run_simulation();
  test.store_to_file();
  if(i==10000 || i==50000 || i==100000)
  {
   cout<<"Trial Number: "<<i<<endl;
   cout<<"Total number of successes: "<<test.successes<<endl;
   test.print_results();

   cout<<"Continue? ";
   for(;;)
   { 
    cin>>next;
    if(next)
    { 
     next = false;
     break;
    }
   }
  }
 }
 return 0;
}
   

