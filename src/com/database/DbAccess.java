/** 
 * This class handles all database access needs with methods that query the database and return the results in a structed format
 * (ex. ArrayList)
 * 
 * @author Hannah Wilder (with code borrowed from http://blog.softeq.com/2012/12/using-pre-populated-sqlite-database-in.html)
 * @version 1.1
 */

package com.database;

import java.util.ArrayList;
import java.util.HashMap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbAccess {

	private static final String DB_NAME = "careerFairDB.db";
	private static ArrayList<String> majorNames;
	private static ArrayList<String> majorAbbrevs;
	private static ArrayList<String> lastFilteredNames = new ArrayList<String>();
	private static ArrayList<String> lastSearchedNames = new ArrayList<String>();
	private static HashMap<String, ArrayList<Major>> majorMap;
	private static HashMap<String, ArrayList<String>> workAuthMap;
	private static HashMap<String, ArrayList<String>> positionMap;
	
	private static ArrayList<String> lastFilteredNamesBlank = new ArrayList<String>();
	private static ArrayList<Company> lastFilteredBlank = new ArrayList<Company>();
	private static ArrayList<String> lastFilteredNamesNotBlank = new ArrayList<String>();
	private static ArrayList<Company> lastFilteredNotBlank = new ArrayList<Company>();
	

	public static ArrayList<String> getSearchedCompanyNames() {
		return (ArrayList<String>)lastSearchedNames.clone();
	}
	/**
	 * getFilteredNamesSep
	 * @param getBlanks - set to true if you want the filtered names that had a blank filter, false otherwise
	 * @return an ArrayList<String> of the company names last filtered that either had (if true) or did not have (if false) a blank for a filtered value 
	 */
	public static ArrayList<String> getFilteredNamesSep(boolean getBlanks) {
		if (getBlanks) {
			return (ArrayList<String>)lastFilteredNamesBlank.clone();
		} else {
			return (ArrayList<String>)lastFilteredNamesNotBlank.clone();
		}
	}
	
	/**
	 * getFilteredSep
	 * @param getBlanks - set to true if you want the filtered companies that had a blank filter, false otherwise
	 * @return an ArrayList<Company> of the companies last filtered that either had (if true) or did not have (if false) a blank for a filtered value 
	 */
	public static ArrayList<Company> getFilteredSep(boolean getBlanks) {
		if (getBlanks) {
			return (ArrayList<Company>)lastFilteredBlank.clone();
		} else {
			return (ArrayList<Company>)lastFilteredNotBlank.clone();
		}
	}
	
	/**
	 * fillPositionMap - gets all the positions each company is
	 * hiring for and places them into a map for later retrieval 
	 *
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 */
	private static void fillPositionMap(SQLiteDatabase database) {
		positionMap = new HashMap<String, ArrayList<String>>();

		Cursor positionsCursor = database
				.rawQuery(
						"SELECT DISTINCT company.name, employmentType.type FROM company, companyToType, employmentType WHERE company._id=companyToType.companyID AND companyToType.typeID=employmentType._id AND type<>'';",
						new String[0]);
		positionsCursor.moveToFirst();
		if (!positionsCursor.isAfterLast()) {
			do {

				ArrayList<String> list = positionMap.get(positionsCursor
						.getString(0));
				if (list == null) {
					ArrayList<String> newlist = new ArrayList<String>();
					newlist.add(positionsCursor.getString(1));
					positionMap.put(positionsCursor.getString(0), newlist);
				} else {
					list.add(positionsCursor.getString(1));
				}
			} while (positionsCursor.moveToNext());
		}
		positionsCursor.close();

	}

	/**
	 * fillWorkAuthMap - gets all the work authorizations each company is
	 * hiring for and places them into a map for later retrieval 
	 * 
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 */
	private static void fillWorkAuthMap(SQLiteDatabase database) {
		workAuthMap = new HashMap<String, ArrayList<String>>();

		Cursor workAuthsCursor = database
				.rawQuery(
						"SELECT DISTINCT company.name, workAuth.type FROM company, companyToWorkAuth, workAuth WHERE company._id=companyToWorkAuth.companyID AND companyToWorkAuth.workAuthID=workAuth._id AND type<>'';",
						new String[0]);
		workAuthsCursor.moveToFirst();
		if (!workAuthsCursor.isAfterLast()) {
			do {

				ArrayList<String> list = workAuthMap.get(workAuthsCursor
						.getString(0));
			
				if (list == null) {
					ArrayList<String> newlist = new ArrayList<String>();
					newlist.add(workAuthsCursor.getString(1));
					String companyName = workAuthsCursor.getString(0);
					workAuthMap.put(workAuthsCursor.getString(0), newlist);
				} else {
					list.add(workAuthsCursor.getString(1));
				}
			} while (workAuthsCursor.moveToNext());
		}
		workAuthsCursor.close();

	}

	/**
	 * fillMajorMap - gets all the majors each company is
	 * hiring for and places them into a map for later retrieval 
	 * 
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 */
	private static void fillMajorMap(SQLiteDatabase database) {
		majorMap = new HashMap<String, ArrayList<Major>>();
		Cursor majorsCursor = database
				.rawQuery(
						"SELECT DISTINCT company.name, major.name, major.abbreviation FROM company, companyToMajor, major WHERE company._id=companyToMajor.companyID AND companyToMajor.majorID=major._id ORDER BY major.abbreviation;",
						new String[0]);
		majorsCursor.moveToFirst();
		if (!majorsCursor.isAfterLast()) {
			do {

				String majorName = majorsCursor.getString(1);
				String majorAbbrev = majorsCursor.getString(2);
				ArrayList<Major> list = majorMap.get(majorsCursor.getString(0));
				if (list == null) {
					ArrayList<Major> newlist = new ArrayList<Major>();
					newlist.add(new Major(majorName, majorAbbrev));
					majorMap.put(majorsCursor.getString(0), newlist);
				} else {
					list.add(new Major(majorName, majorAbbrev));
				}

			} while (majorsCursor.moveToNext());
		}
		majorsCursor.close();

	}

	/**
	 * Queries the database to obtain a list of company names and fill an array
	 * list with them
	 * 
	 * @param companies
	 *            - an empty array list to fill with company names
	 * @param database
	 *            - the database to query (obtain database with
	 *            ExternalDbOpenHelper instance)
	 */
	public static void fillCompanies(ArrayList companies,
			SQLiteDatabase database) {
		// companies = new ArrayList<String>();
		Cursor companiesCursor = database
				.rawQuery(
						"SELECT DISTINCT company.name FROM company ORDER BY replace(replace(lower(company.name), '.', ''), ' ', '');",
						new String[0]);
		companiesCursor.moveToFirst();
		if (!companiesCursor.isAfterLast()) {
			do {
				String name = companiesCursor.getString(0);
				companies.add(name);
			} while (companiesCursor.moveToNext());
		}
		companiesCursor.close();
	}

	/**
	 * Returns a list of the companies that were in the set last filtered by
	 * getCompaniesWith
	 * 
	 * @param database
	 *            - the database to query (obtain database with
	 *            ExternalDbOpenHelper instance)
	 * @return the names of the companies in the list last returned by
	 *         getCompaniesWith
	 */
	public static ArrayList<String> getFilteredNames(SQLiteDatabase database) {
		if (lastFilteredNames.isEmpty()) {
			ArrayList<String> toReturn = new ArrayList<String>();
			fillCompanies(toReturn, database);
			return toReturn;
		} else {
			return lastFilteredNames;
		}
	}

	/**
	 * getAllCompanies - gets all the companies in the database, ordered by
	 * company name, case insensitive, ignores spaces and periods in the names
	 * 
	 * @param companies
	 *            - an ArrayList to fill with companies
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 */
	public static void getAllCompanies(ArrayList<Company> companies,
			SQLiteDatabase database) {
		Cursor companiesCursor = database
				.rawQuery(
						"SELECT DISTINCT company.name, company.website, location.tableNum, room.name FROM company, companyToLocation, location, room WHERE company._id=companyToLocation.companyID AND companyToLocation.locationID=location._id AND location.roomID=room._id ORDER BY replace(replace(lower(replace(company.name, 'The ', '')), '.', ''), ' ', '');",
						new String[0]);
		companiesCursor.moveToFirst();
		if (!companiesCursor.isAfterLast()) {
			do {
				String name = companiesCursor.getString(0);
				String website = companiesCursor.getString(1);
				String tableNum = companiesCursor.getString(2);
				String room = companiesCursor.getString(3);

				if (majorMap == null) {
					fillMajorMap(database);
				}
				if (positionMap == null) {
					fillPositionMap(database);
				}
				if (workAuthMap == null) {
					fillWorkAuthMap(database);
				}
				ArrayList<Major> majorList = majorMap.get(name);
				ArrayList<String> positionList = positionMap.get(name);
				ArrayList<String> workAuthList = workAuthMap.get(name);
				if (majorList == null) {
					majorList = new ArrayList<Major>();
				}
				if (positionList == null) {
					positionList = new ArrayList<String>();
				}
				if (workAuthList == null) {
					workAuthList = new ArrayList<String>();
				}
				Company newCompany = new Company(name, website, tableNum, room,
						majorList, positionList, workAuthList);

				companies.add(newCompany);

			} while (companiesCursor.moveToNext());
		}
		companiesCursor.close();
	}

	/**
	 * getAllCompanies - gets all the companies in the database
	 * 
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @return an ArrayList filled with companies
	 */
	public static ArrayList<Company> getAllCompanies(SQLiteDatabase database) {
		ArrayList<Company> companies = new ArrayList<Company>();
		getAllCompanies(companies, database);
		return companies;
	}
	
	public static ArrayList<Company> searchAllCompanies(String filterName, SQLiteDatabase database) {
		lastSearchedNames = new ArrayList<String>();
		ArrayList<Company> companies = new ArrayList<Company>();
		String fromString = "FROM company, companyToLocation, location, room";
		String whereString = "WHERE company._id=companyToLocation.companyID AND companyToLocation.locationID=location._id AND location.roomID=room._id";


		// Handle name filter
		if (!filterName.isEmpty()) {
			filterName = "%" + filterName + "%";
			whereString = whereString + " AND company.name LIKE '" + filterName
					+ "'";
		}

		// Toss strings into array to be passed into query
		String[] query = { fromString, whereString };
		String wholeQuery = "SELECT DISTINCT company.name, company.website, location.tableNum, room.name "
				+ fromString
				+ " "
				+ whereString
				+ " ORDER BY replace(replace(lower(replace(company.name, 'The ', '')), '.', ''), ' ', '');";
		wholeQuery = wholeQuery + "";
		Cursor companiesCursor = database.rawQuery(wholeQuery, new String[0]);
		companiesCursor.moveToFirst();
		if (!companiesCursor.isAfterLast()) {
			do {
				String name = companiesCursor.getString(0);
				String website = companiesCursor.getString(1);
				String tableNum = companiesCursor.getString(2);
				String room = companiesCursor.getString(3);

				lastSearchedNames.add(name);

				if (majorMap == null) {
					fillMajorMap(database);
				}
				if (positionMap == null) {
					fillPositionMap(database);
				}
				if (workAuthMap == null) {
					fillWorkAuthMap(database);
				}
				ArrayList<Major> majorList = majorMap.get(name);
				ArrayList<String> positionList = positionMap.get(name);
				ArrayList<String> workAuthList = workAuthMap.get(name);
				if (majorList == null) {
					majorList = new ArrayList<Major>();
				}
				if (positionList == null) {
					positionList = new ArrayList<String>();
				}
				if (workAuthList == null) {
					workAuthList = new ArrayList<String>();
				}

				Company newCompany = new Company(name, website, tableNum, room,
						majorList, positionList, workAuthList);
				companies.add(newCompany);

			} while (companiesCursor.moveToNext());
		}
		companiesCursor.close();

		return companies;
	}

	/**
	 * getCompaniesWith - gets all the companies in the database fitting a
	 * specific set of criteria
	 * 
	 * @param filterRoom
	 *            - one of either "Wood", "Multipurpose", "Hall" or ""
	 *            (indicating not to filter at all)
	 * @param filterMajor
	 *            - an ArrayList of majors to filter by, this filter will find
	 *            all companies with at least one of the specified majors
	 * @param filterWorkAuth
	 *            - an ArrayList of strings to filter work authorizations by,
	 *            this filter will find all the companies with at least one of
	 *            the specified work authorizations
	 * @param filterPosition
	 *            - an ArrayList of strings to filter position types by, this
	 *            filter will find all the companies with at least one of the
	 *            specified position types
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 */
	public static ArrayList<Company> getCompaniesWith(
			String filterRoom, ArrayList<String> filterMajor,
			ArrayList<String> filterWorkAuth, ArrayList<String> filterPosition,
			SQLiteDatabase database) {
		ArrayList<Company> companies = new ArrayList<Company>();
		lastFilteredNames = new ArrayList<String>();
		lastFilteredNamesBlank = new ArrayList<String>();
		lastFilteredBlank = new ArrayList<Company>();
		
		lastFilteredNamesNotBlank = new ArrayList<String>();
		lastFilteredNotBlank = new ArrayList<Company>();
		// Set default from and where strings (if there are no filters
		// specified)
		String fromString = "FROM company, companyToLocation, location, room";
		String whereString = "WHERE company._id=companyToLocation.companyID AND companyToLocation.locationID=location._id AND location.roomID=room._id";

		if (!filterRoom.isEmpty()) {
			whereString = whereString + " AND room.name = '" + filterRoom + "'";
		}

		// Handle Major filter
		if (!filterMajor.isEmpty()) {
			String majorSet = "'ALL'";
			for (String major : filterMajor) {

				majorSet = majorSet + ", '" + major + "'";
			}
			whereString = whereString
					+ " AND company._id=companyToMajor.companyID AND major._id=companyToMajor.majorID AND major.abbreviation IN ("
					+ majorSet + ")";
			fromString = fromString + ", companyToMajor, major";
		}

		// Handle workAuth filter
		if (!filterWorkAuth.isEmpty()) {
			String workAuthSet = "''";
			for (String workAuth : filterWorkAuth) {
				workAuthSet = workAuthSet + ", '" + workAuth + "'";
			}
			whereString = whereString
					+ " AND company._id=companyToWorkAuth.companyID AND workAuth._id=companyToWorkAuth.workAuthID AND workAuth.type IN ("
					+ workAuthSet + ")";
			fromString = fromString + ", companyToWorkAuth, workAuth";
		}

		// Handle position filter
		if (!filterPosition.isEmpty()) {
			String positionSet = "''";
			for (String position : filterPosition) {
				positionSet = positionSet + ", '" + position + "'";
			}
			whereString = whereString
					+ " AND company._id=companyToType.companyID AND employmentType._id=companyToType.typeID AND employmentType.type IN ("
					+ positionSet + ")";
			fromString = fromString + ", companyToType, employmentType";
		}

		// Toss strings into array to be passed into query
		String[] query = { fromString, whereString };
		String wholeQuery = "SELECT DISTINCT company.name, company.website, location.tableNum, room.name "
				+ fromString
				+ " "
				+ whereString
				+ " ORDER BY replace(replace(lower(replace(company.name, 'The ', '')), '.', ''), ' ', '');";
		wholeQuery = wholeQuery + "";
		Cursor companiesCursor = database.rawQuery(wholeQuery, new String[0]);
		companiesCursor.moveToFirst();
		if (!companiesCursor.isAfterLast()) {
			do {
				String name = companiesCursor.getString(0);
				String website = companiesCursor.getString(1);
				String tableNum = companiesCursor.getString(2);
				String room = companiesCursor.getString(3);

				lastFilteredNames.add(name);

				if (majorMap == null) {
					fillMajorMap(database);
				}
				if (positionMap == null) {
					fillPositionMap(database);
				}
				if (workAuthMap == null) {
					fillWorkAuthMap(database);
				}
				ArrayList<Major> majorList = majorMap.get(name);
				ArrayList<String> positionList = positionMap.get(name);
				ArrayList<String> workAuthList = workAuthMap.get(name);
				if (majorList == null) {
					majorList = new ArrayList<Major>();
				}
				if (positionList == null) {
					positionList = new ArrayList<String>();
				}
				if (workAuthList == null) {
					workAuthList = new ArrayList<String>();
				}
				
				Company newCompany = new Company(name, website, tableNum, room,
						majorList, positionList, workAuthList);
				companies.add(newCompany);
				
				//Determine if any of the fields matched a blank
				
				
				
				if (!filterMajor.isEmpty() && majorList.isEmpty()) {
					lastFilteredNamesBlank.add(name);
					lastFilteredBlank.add(newCompany);
				} else if (!filterWorkAuth.isEmpty() && workAuthList.isEmpty()) {
					lastFilteredNamesBlank.add(name);
					lastFilteredBlank.add(newCompany);
				} else if (!filterPosition.isEmpty() && positionList.isEmpty()) {
					lastFilteredNamesBlank.add(name);
					lastFilteredBlank.add(newCompany);
				} else {
					lastFilteredNamesNotBlank.add(name);
					lastFilteredNotBlank.add(newCompany);
				}
			} while (companiesCursor.moveToNext());
		}
		companiesCursor.close();

		return companies;
	}

	/**
	 * getMajorsForCompany - gets all the majors a specific company is looking
	 * for
	 * 
	 * @param company
	 *            - the name of the company to get the majors for
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @return an ArrayList filled with majors
	 */
	public static ArrayList<Major> getMajorsForCompany(String company,
			SQLiteDatabase database) {
		String[] nameArray = { company };

		ArrayList<Major> majors = new ArrayList<Major>();
		majorNames = new ArrayList<String>();
		majorAbbrevs = new ArrayList<String>();
		Cursor majorsCursor = database
				.rawQuery(
						"SELECT major.name, major.abbreviation FROM company, companyToMajor, major WHERE company._id=companyToMajor.companyID AND company.name=? AND companyToMajor.majorID=major._id ORDER BY major.abbreviation;",
						nameArray);
		majorsCursor.moveToFirst();
		if (!majorsCursor.isAfterLast()) {
			do {
				String majorName = majorsCursor.getString(0);
				majorNames.add(majorName);
				String majorAbbrev = majorsCursor.getString(1);
				majorAbbrevs.add(majorAbbrev);
				majors.add(new Major(majorName, majorAbbrev));
			} while (majorsCursor.moveToNext());
		}
		majorsCursor.close();

		return majors;
	}

	/**
	 * getPositionsForCompany - gets all the positions a specific company is
	 * hiring for
	 * 
	 * @param company
	 *            - the name of the company to get the positions for
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @return an ArrayList filled with positions
	 */
	public static ArrayList<String> getPositionsForCompany(String company,
			SQLiteDatabase database) {
		String[] nameArray = { company };

		ArrayList<String> positions = new ArrayList<String>();
		Cursor positionsCursor = database
				.rawQuery(
						"SELECT employmentType.type FROM company, companyToType, employmentType WHERE company._id=companyToType.companyID AND company.name=? AND companyToType.typeID=employmentType._id AND type<>'';",
						nameArray);
		positionsCursor.moveToFirst();
		if (!positionsCursor.isAfterLast()) {
			do {
				positions.add(positionsCursor.getString(0));
								
			} while (positionsCursor.moveToNext());
		}
		positionsCursor.close();

		return positions;
	}

	/**
	 * getWorkAuthsForCompany - gets all the work authorization types a specific
	 * company is looking for
	 * 
	 * @param company
	 *            - the name of the company to get the work authorizations for
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @return an ArrayList filled with work authorizations
	 */
	public static ArrayList<String> getWorkAuthsForCompany(String company,
			SQLiteDatabase database) {
		String[] nameArray = { company };

		ArrayList<String> workAuths = new ArrayList<String>();
		Cursor workAuthsCursor = database
				.rawQuery(
						"SELECT workAuth.type FROM company, companyToWorkAuth, workAuth WHERE company._id=companyToWorkAuth.companyID AND company.name=? AND companyToWorkAuth.workAuthID=workAuth._id AND type<>'';",
						nameArray);
		workAuthsCursor.moveToFirst();
		if (!workAuthsCursor.isAfterLast()) {
			do {
				workAuths.add(workAuthsCursor.getString(0));
			} while (workAuthsCursor.moveToNext());
		}
		workAuthsCursor.close();

		return workAuths;
	}

	/**
	 * getAllMajors - gets a list of all the majors
	 * 
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @param orderByName
	 *            - orders by name if true, else ordered by abbreviation
	 * @return an ArrayList filled with major objects
	 */
	public static ArrayList<Major> getAllMajors(SQLiteDatabase database,
			boolean orderByName) {
		String[] empty = {};

		ArrayList<Major> majors = new ArrayList<Major>();
		Cursor majorsCursor;
		if (orderByName) {
			majorsCursor = database.rawQuery(
					"SELECT name, abbreviation FROM major ORDER BY name;",
					empty);
		} else {
			majorsCursor = database
					.rawQuery(
							"SELECT name, abbreviation FROM major ORDER BY abbreviation;",
							empty);
		}

		majorsCursor.moveToFirst();
		if (!majorsCursor.isAfterLast()) {
			do {
				String majorName = majorsCursor.getString(0);
				String majorAbbrev = majorsCursor.getString(1);
				if (!majorName.equals("")) {
					majors.add(new Major(majorName, majorAbbrev));
				}
			} while (majorsCursor.moveToNext());
		}
		majorsCursor.close();

		return majors;
	}

	/**
	 * getAllMajorNames - gets a list of all the majors
	 * 
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @return an ArrayList filled with the major names ordered by name
	 */
	public static ArrayList<String> getAllMajorNames(SQLiteDatabase database) {
		String[] empty = {};

		ArrayList<String> names = new ArrayList<String>();
		Cursor majorsCursor = database.rawQuery(
				"SELECT name FROM major ORDER BY name;", empty);

		majorsCursor.moveToFirst();
		if (!majorsCursor.isAfterLast()) {
			do {
				names.add(majorsCursor.getString(0));
			} while (majorsCursor.moveToNext());
		}
		majorsCursor.close();

		return names;
	}

	/**
	 * getAllMajorNames - gets a list of all the majors
	 * 
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @return an ArrayList filled with the major abbreviations ordered by abbreviation
	 */
	public static ArrayList<String> getAllMajorAbbrevs(SQLiteDatabase database) {
		String[] empty = {};

		ArrayList<String> abbrev = new ArrayList<String>();
		Cursor majorsCursor = database.rawQuery(
				"SELECT abbreviation FROM major ORDER BY abbreviation;", empty);

		majorsCursor.moveToFirst();
		if (!majorsCursor.isAfterLast()) {
			do {
				abbrev.add(majorsCursor.getString(0));
			} while (majorsCursor.moveToNext());
		}
		majorsCursor.close();

		return abbrev;
	}

	/**
	 * getAllWorkAuths - gets a list of all the work authorizations
	 * 
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @return an ArrayList filled with the work authorizations
	 */
	public static ArrayList<String> getAllWorkAuths(SQLiteDatabase database) {
		String[] empty = {};

		ArrayList<String> workAuths = new ArrayList<String>();
		Cursor workAuthsCursor = database.rawQuery(
				"SELECT type FROM workAuth ORDER BY type;", empty);
		workAuthsCursor.moveToFirst();
		if (!workAuthsCursor.isAfterLast()) {
			do {
				if(!workAuthsCursor.getString(0).equals("") )
					workAuths.add(workAuthsCursor.getString(0));
			} while (workAuthsCursor.moveToNext());
		}
		workAuthsCursor.close();

		return workAuths;
	}

	/**
	 * getAllPositions - gets a list of all the positions/employment types
	 * 
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @return an ArrayList filled with the positions
	 */
	public static ArrayList<String> getAllPositions(SQLiteDatabase database) {
		String[] empty = {};

		ArrayList<String> positions = new ArrayList<String>();
		Cursor positionsCursor = database.rawQuery(
				"SELECT type FROM employmentType ORDER BY type;", empty);
		positionsCursor.moveToFirst();
		if (!positionsCursor.isAfterLast()) {
			do {
				if (!positionsCursor.getString(0).equals("")) {
					positions.add(positionsCursor.getString(0));
				}
				
			} while (positionsCursor.moveToNext());
		}

		positionsCursor.close();

		return positions;

	}

	/**
	 * getTableCompanyMap
	 * 
	 * Returns a hash map keyed with the table number for all the companies in
	 * the wood or multipurpose gyms
	 * 
	 * @param WoodGym
	 *            set to true to retrieve the wood gym companies, false to
	 *            retrieve multipurpose room companies
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @return a hash map with string table number keys to the company objects
	 */
	public static HashMap<String, Company> getTableCompanyMap(boolean WoodGym,
			SQLiteDatabase database) {
		HashMap<String, Company> map = new HashMap<String, Company>();
		String[] empty = {};

		String roomName;
		if (WoodGym) {
			roomName = "Wood";
		} else {
			roomName = "Multipurpose";
		}

		ArrayList<Company> companies = getAllCompanies(database);

		for (Company company : companies) {
			if (company.getRoom().equals(roomName)) {
				map.put(company.getTableNum(), company);
			}
		}

		return map;
	}

	/**
	 * 
	 * @param key
	 *            - this parameter tells the method what to use for the key,
	 *            options are as follows 0 - key with company name 1 - key with
	 *            table number otherwise return null (may request for other keys
	 *            if needed)
	 * @param companyList
	 *            - an ArrayList with the company objects to be inserted into
	 *            the hashmap
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @return a hashmap with the company objects, keyed with the requested
	 *         field, or null if the requested field was invalid
	 */
	public static HashMap<String, Company> getHashMapForCompanyList(int key,
			ArrayList<Company> companyList, SQLiteDatabase database) {
		if (key == 0) {
			HashMap<String, Company> map = new HashMap<String, Company>();
			for (Company company : companyList) {
				map.put(company.getName(), company);
			}
			return map;
		} else if (key == 1) {
			HashMap<String, Company> map = new HashMap<String, Company>();
			for (Company company : companyList) {
				map.put(company.getTableNum(), company);
			}
			return map;
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param key
	 *            - this parameter tells the method what to use for the key,
	 *            options are as follows 0 - key with major name 1 - key with
	 *            major abbrev otherwise return null (may request for other keys
	 *            if needed)
	 * @param MajorList
	 *            - an ArrayList with the company objects to be inserted into
	 *            the hashmap
	 * @param database
	 *            - SQLite database object returned by
	 *            ExternalDbOpenHelper.openDataBase
	 * @return a hashmap with the major objects, keyed with the requested field,
	 *         or null if the requested field was invalid
	 */
	public static HashMap<String, Major> getHashMapForMajorList(int key,
			ArrayList<Major> MajorList, SQLiteDatabase database) {
		if (key == 0) {
			HashMap<String, Major> map = new HashMap<String, Major>();
			for (Major major : MajorList) {
				map.put(major.getName(), major);
			}
			return map;
		} else if (key == 1) {
			HashMap<String, Major> map = new HashMap<String, Major>();
			for (Major major : MajorList) {
				map.put(major.getAbbrev(), major);
			}
			return map;
		} else {
			return null;
		}
	}
}
